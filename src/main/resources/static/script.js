const app = document.getElementById("app");

const state = {
    route: "home",
    authMode: "login",
    token: localStorage.getItem("authToken"),
    userId: Number(localStorage.getItem("userId")) || null,
    me: null,
    users: [],
    contacts: [],
    chats: [],
    contactInvitationsIn: [],
    contactInvitationsOut: [],
    chatInvitationsIn: [],
    chatInvitationsOut: [],
    notifications: [],
    selectedChatId: null,
    messages: {},
    message: "",
    messageType: "error",
    socket: null,
    socketReconnectTimer: null,
    loading: false,
    designColors: null,
    settings: {
        notifications: true,
        compactMode: false,
        sound: false
    }
};

const routes = [
    { id: "home", label: "Главная", icon: "⌂" },
    { id: "contacts", label: "Контакты", icon: "@" },
    { id: "chats", label: "Чаты", icon: "◫" },
    { id: "settings", label: "Настройки", icon: "⚙" },
    { id: "profile", label: "Профиль", icon: "◯" }
];

function setMessage(text, type = "error") {
    state.message = text;
    state.messageType = type;
    render();
}

async function loadDesignSettings() {
    try {
        state.designColors = await api("/web/design-settings", {
            authRequired: false,
            sendAuth: false
        });
        applyDesignColors(state.designColors);
    } catch (error) {
        console.warn("Design settings were not loaded", error);
    }
}

function applyDesignColors(colors) {
    if (!colors) return;

    const root = document.documentElement;
    const map = {
        primary: "--primary",
        secondary: "--secondary",
        warning: "--warning",
        error: "--error",
        success: "--success"
    };

    Object.entries(map).forEach(([key, cssVariable]) => {
        if (colors[key]) root.style.setProperty(cssVariable, colors[key]);
    });

    if (colors.error) root.style.setProperty("--danger", colors.error);
}

async function api(path, options = {}) {
    const authRequired = options.authRequired !== false;
    const sendAuth = options.sendAuth !== false;
    const isMultipart = options.body instanceof FormData;
    const requestOptions = { ...options };
    delete requestOptions.authRequired;
    delete requestOptions.sendAuth;

    const headers = {
        ...(requestOptions.headers || {})
    };

    if (!isMultipart) {
        headers["Content-Type"] = "application/json";
    }

    if (sendAuth && state.token) {
        headers.Authorization = `Bearer ${state.token}`;
    }

    const response = await fetch(path, {
        ...requestOptions,
        headers
    });

    const text = await response.text();
    const data = text ? safeJson(text) : null;
    const message = typeof data === "string" ? data : data?.message || text || "Ошибка запроса";

    if (!response.ok) {
        const error = new Error(message);
        error.status = response.status;

        if (response.status === 401 && authRequired) {
            logout(false);
            error.message = "Сессия истекла. Войдите снова.";
        }

        throw error;
    }

    return data;
}

function safeJson(text) {
    try {
        return JSON.parse(text);
    } catch {
        return text;
    }
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

function fmtDate(value) {
    if (!value) return "—";
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return value;
    return date.toLocaleString("ru-RU", { dateStyle: "short", timeStyle: "short" });
}

async function bootstrap() {
    await loadDesignSettings();

    if (!state.token) {
        render();
        return;
    }

    state.loading = true;
    render();

    try {
        state.me = await api("/web/me");
        state.userId = state.me.id;
        localStorage.setItem("userId", String(state.me.id));
        await loadDashboardData();
        connectWebSocket();
    } catch (error) {
        state.message = error.message;
        state.token = null;
        localStorage.removeItem("authToken");
        localStorage.removeItem("userId");
    } finally {
        state.loading = false;
        render();
    }
}

function connectWebSocket() {
    if (!state.token) return;
    if (state.socket && state.socket.readyState === WebSocket.OPEN) return;
    if (state.socketReconnectTimer) {
        clearTimeout(state.socketReconnectTimer);
        state.socketReconnectTimer = null;
    }

    const protocol = window.location.protocol === "https:" ? "wss" : "ws";
    const url = `${protocol}://${window.location.host}/ws?token=${encodeURIComponent(state.token)}`;
    const socket = new WebSocket(url);
    state.socket = socket;

    socket.onmessage = async (event) => {
        const payload = safeJson(event.data);
        if (!payload || typeof payload !== "object") return;

        if (payload.event === "message:new" && payload.data) {
            applySocketMessage(payload.data);
        }

        if ((payload.event === "invitation:created" || payload.event === "invitation:updated") && payload.data) {
            await applySocketInvitation(payload.data, payload.event);
        }

        if (payload.event === "notification:new" && payload.data) {
            addNotification(payload.data);
        }
    };

    socket.onclose = () => {
        if (state.socket === socket) state.socket = null;
        if (state.token) {
            state.socketReconnectTimer = setTimeout(connectWebSocket, 2000);
        }
    };

    socket.onerror = () => {
        socket.close();
    };
}

function reconnectWebSocket() {
    if (!state.token) return;
    if (state.socket) {
        state.socket.close();
        return;
    }
    connectWebSocket();
}

function applySocketMessage(message) {
    const chatId = Number(message.idChat);
    if (!chatId) return;

    const messages = state.messages[chatId] || [];
    if (messages.some(existing => existing.id === message.id)) return;

    state.messages[chatId] = [...messages, {
        ...message,
        isMine: Number(message.senderUserId) === Number(state.userId)
    }].sort((left, right) => new Date(left.createdAt) - new Date(right.createdAt));

    if (state.route === "chats") render();
}

async function applySocketInvitation(data, eventName) {
    const kind = data.kind;
    const direction = data.direction;
    const invitation = data.invitation;
    if (!kind || !direction || !invitation) return;

    const incomingKey = kind === "contact" ? "contactInvitationsIn" : "chatInvitationsIn";
    const outgoingKey = kind === "contact" ? "contactInvitationsOut" : "chatInvitationsOut";
    const targetKey = direction === "outgoing" ? outgoingKey : incomingKey;
    const oppositeKey = direction === "outgoing" ? incomingKey : outgoingKey;

    upsertInvitation(targetKey, invitation);
    removeInvitation(oppositeKey, invitation.id);

    if (eventName === "invitation:updated" && invitation.status !== "pending") {
        removeInvitation(incomingKey, invitation.id);
        removeInvitation(outgoingKey, invitation.id);
        await loadDashboardData();
        if (kind === "chat" && invitation.status === "accepted") reconnectWebSocket();
    }

    render();
}

function upsertInvitation(key, invitation) {
    const list = state[key] || [];
    const index = list.findIndex(item => item.id === invitation.id);
    if (invitation.status && invitation.status !== "pending") {
        state[key] = list.filter(item => item.id !== invitation.id);
        return;
    }
    state[key] = index >= 0
        ? list.map(item => item.id === invitation.id ? invitation : item)
        : [invitation, ...list];
}

function removeInvitation(key, id) {
    state[key] = (state[key] || []).filter(item => item.id !== id);
}

function addNotification(notification) {
    const item = {
        id: `${Date.now()}-${Math.random().toString(16).slice(2)}`,
        title: notification.title || "Уведомление",
        message: notification.message || "",
        type: notification.type || "info",
        kind: notification.kind || null,
        createdAt: new Date().toISOString()
    };

    state.notifications = [item, ...state.notifications].slice(0, 10);
    if (state.settings.notifications) {
        state.message = item.message || item.title;
        state.messageType = "success";
    }
    render();
}

async function loadDashboardData() {
    await Promise.allSettled([
        loadUsers(),
        loadContacts(),
        loadChats(),
        loadContactInvitations(),
        loadChatInvitations()
    ]);
}

async function loadUsers(query = "") {
    const suffix = query ? `?name=${encodeURIComponent(query)}` : "";
    state.users = await api(`/web/users${suffix}`).catch(() => []);
}

async function loadContacts() {
    state.contacts = await api("/web/contacts").catch(() => []);
}

async function loadChats() {
    state.chats = await api("/web/chats").catch(() => []);
    if (state.selectedChatId && !state.chats.some(chat => chat.id === state.selectedChatId)) {
        state.selectedChatId = null;
    }
    if (!state.selectedChatId && state.chats.length > 0) {
        state.selectedChatId = state.chats[0].id;
    }
    if (state.selectedChatId) {
        await loadMessages(state.selectedChatId);
    }
}

async function loadMessages(chatId) {
    if (!chatId) return;
    state.messages[chatId] = await api(`/web/chats/${chatId}/messages`).catch(() => []);
}

async function loadContactInvitations() {
    const [incoming, outgoing] = await Promise.all([
        api("/web/contact-invitations?direction=incoming").catch(() => []),
        api("/web/contact-invitations?direction=outgoing").catch(() => [])
    ]);
    state.contactInvitationsIn = incoming;
    state.contactInvitationsOut = outgoing;
}

async function loadChatInvitations() {
    const [incoming, outgoing] = await Promise.all([
        api("/web/chat-invitations?direction=incoming").catch(() => []),
        api("/web/chat-invitations?direction=outgoing").catch(() => [])
    ]);
    state.chatInvitationsIn = incoming;
    state.chatInvitationsOut = outgoing;
}

async function login(form) {
    const body = Object.fromEntries(new FormData(form).entries());
    state.loading = true;
    render();

    try {
        const result = await api("/authentication", {
            method: "POST",
            authRequired: false,
            sendAuth: false,
            body: JSON.stringify(body)
        });

        state.token = result.authToken;
        state.userId = result.id;
        localStorage.setItem("authToken", result.authToken);
        localStorage.setItem("userId", String(result.id));
        state.route = "home";
        state.message = "";
        await bootstrap();
    } catch (error) {
        state.loading = false;
        setMessage(error.status === 401 ? "Неверный логин или пароль." : error.message);
    }
}

async function register(form) {
    const body = Object.fromEntries(new FormData(form).entries());
    state.loading = true;
    render();

    try {
        const result = await api("/register", {
            method: "POST",
            authRequired: false,
            sendAuth: false,
            body: JSON.stringify(body)
        });

        state.token = result.authToken;
        state.userId = result.id;
        localStorage.setItem("authToken", result.authToken);
        localStorage.setItem("userId", String(result.id));
        state.route = "home";
        state.message = "";
        await bootstrap();
    } catch (error) {
        state.loading = false;
        setMessage(error.message);
    }
}

function logout(renderAfter = true) {
    if (state.socket) {
        state.socket.close();
        state.socket = null;
    }
    if (state.socketReconnectTimer) {
        clearTimeout(state.socketReconnectTimer);
        state.socketReconnectTimer = null;
    }
    state.token = null;
    state.userId = null;
    state.me = null;
    state.route = "home";
    localStorage.removeItem("authToken");
    localStorage.removeItem("userId");
    if (renderAfter) render();
}

async function updateProfile(form) {
    const body = Object.fromEntries(new FormData(form).entries());
    try {
        state.me = await api("/web/me", {
            method: "PUT",
            body: JSON.stringify(body)
        });
        setMessage("Профиль обновлен", "success");
    } catch (error) {
        setMessage(error.message);
    }
}

async function sendContactInvitation(form) {
    const body = Object.fromEntries(new FormData(form).entries());
    try {
        await api("/web/contact-invitations", {
            method: "POST",
            body: JSON.stringify({
                receiverUserId: Number(body.receiverUserId),
                message: body.message || null
            })
        });
        await loadContactInvitations();
        setMessage("Приглашение отправлено", "success");
    } catch (error) {
        setMessage(error.message);
    }
}

async function sendChatInvitation(form) {
    const body = Object.fromEntries(new FormData(form).entries());
    try {
        await api("/web/chat-invitations", {
            method: "POST",
            body: JSON.stringify({
                idChat: Number(body.idChat),
                inviteeUserId: Number(body.inviteeUserId),
                message: body.message || null
            })
        });
        await loadChatInvitations();
        setMessage("Приглашение в чат отправлено", "success");
    } catch (error) {
        setMessage(error.message);
    }
}

async function createChat(form) {
    const body = Object.fromEntries(new FormData(form).entries());
    try {
        const chat = await api("/web/chats", {
            method: "POST",
            body: JSON.stringify({
                name: body.name
            })
        });
        state.selectedChatId = chat.id;
        await loadChats();
        reconnectWebSocket();
        setMessage("Чат создан", "success");
    } catch (error) {
        setMessage(error.message);
    }
}

async function sendMessage(form) {
    const selectedChatId = Number(form.dataset.chatId);
    const body = Object.fromEntries(new FormData(form).entries());
    const files = Array.from(form.elements.mediaFiles?.files || []);

    try {
        const mediaFiles = files.length ? await uploadMediaFiles(files) : [];
        await api(`/web/chats/${selectedChatId}/messages`, {
            method: "POST",
            body: JSON.stringify({
                value: body.value || "",
                type: "text",
                mediaFileIds: mediaFiles.map(file => file.id)
            })
        });
        form.reset();
        await loadMessages(selectedChatId);
        render();
    } catch (error) {
        setMessage(error.message);
    }
}

async function uploadMediaFiles(files) {
    const uploaded = [];
    for (const file of files) {
        const formData = new FormData();
        formData.append("file", file);
        uploaded.push(await api("/web/media", {
            method: "POST",
            body: formData
        }));
    }
    return uploaded;
}

async function invitationAction(kind, id, action) {
    const base = kind === "contact" ? "/web/contact-invitations" : "/web/chat-invitations";
    try {
        await api(`${base}/${id}/${action}`, { method: "POST" });
        await loadDashboardData();
        reconnectWebSocket();
        setMessage("Статус приглашения обновлен", "success");
    } catch (error) {
        setMessage(error.message);
    }
}

function setRoute(route) {
    state.route = route;
    state.message = "";
    render();
}

function renderAuth() {
    const isLogin = state.authMode === "login";
    app.innerHTML = `
        <main class="auth-layout">
            <section class="brand-panel">
                <div>
                    <div class="brand-lockup">
                        <img class="brand-logo" src="/static/logo.png" alt="Messenger Connect">
                        <span>Messenger Connect</span>
                    </div>
                    <h1>Общение, контакты и чаты в одном браузере</h1>
                    <p>Веб-версия использует текущее REST API сервера и готовится к полноценной работе с контактами, приглашениями и чатами.</p>
                </div>
                <p class="muted">Безопасный доступ через токен авторизации.</p>
            </section>
            <section class="auth-card-wrap">
                <div class="auth-card">
                    <h2>${isLogin ? "Вход" : "Регистрация"}</h2>
                    <p class="hint">${isLogin ? "Введите логин и пароль." : "Создайте аккаунт пользователя мессенджера."}</p>
                    <form class="form" data-form="${isLogin ? "login" : "register"}">
                        ${isLogin ? loginFields() : registerFields()}
                        <button class="btn" type="submit" ${state.loading ? "disabled" : ""}>${state.loading ? "Подождите..." : isLogin ? "Войти" : "Зарегистрироваться"}</button>
                        <div class="message ${state.messageType === "success" ? "success" : ""}">${escapeHtml(state.message)}</div>
                    </form>
                    <div class="button-row">
                        <button class="btn ghost" data-auth-mode="${isLogin ? "register" : "login"}">
                            ${isLogin ? "Создать аккаунт" : "Уже есть аккаунт"}
                        </button>
                    </div>
                </div>
            </section>
        </main>
    `;
}

function loginFields() {
    return `
        <label class="field"><span>Логин</span><input class="input" name="login" autocomplete="username" required></label>
        <label class="field"><span>Пароль</span><input class="input" name="password" type="password" autocomplete="current-password" required></label>
    `;
}

function registerFields() {
    return `
        <label class="field"><span>Имя</span><input class="input" name="username" autocomplete="name" required></label>
        <label class="field"><span>ФИО</span><input class="input" name="fio" required></label>
        <label class="field"><span>Email</span><input class="input" name="email" type="email" required></label>
        <label class="field"><span>Телефон</span><input class="input" name="phone" placeholder="+79991234567" required></label>
        <label class="field"><span>Логин</span><input class="input" name="login" autocomplete="username" required></label>
        <label class="field"><span>Пароль</span><input class="input" name="password" type="password" autocomplete="new-password" required></label>
    `;
}

function renderApp() {
    app.innerHTML = `
        <main class="layout">
            <aside class="sidebar">
                <div class="brand-lockup">
                    <img class="brand-logo" src="/static/logo.png" alt="Messenger Connect">
                    <span>Messenger Connect</span>
                </div>
                <nav class="nav">
                    ${routes.map(route => `
                        <button class="nav-btn ${state.route === route.id ? "active" : ""}" data-route="${route.id}">
                            <span>${route.icon}</span><span>${route.label}</span>
                        </button>
                    `).join("")}
                </nav>
                <div class="sidebar-footer">
                    <div class="user-mini">
                        <strong>${escapeHtml(state.me?.name || "Пользователь")}</strong>
                        <span class="muted">${escapeHtml(state.me?.login || "")}</span>
                    </div>
                    <button class="btn danger" data-action="logout">Выйти</button>
                </div>
            </aside>
            <section class="content">
                ${renderView()}
                ${state.message ? `<p class="message ${state.messageType === "success" ? "success" : ""}">${escapeHtml(state.message)}</p>` : ""}
            </section>
        </main>
    `;
}

function topbar(title, subtitle, action = "") {
    return `
        <div class="topbar">
            <div class="view-title">
                <h1>${title}</h1>
                <p>${subtitle}</p>
            </div>
            ${action}
        </div>
    `;
}

function renderView() {
    switch (state.route) {
        case "contacts":
            return renderContacts();
        case "chats":
            return renderChats();
        case "settings":
            return renderSettings();
        case "profile":
            return renderProfile();
        default:
            return renderHome();
    }
}

function renderHome() {
    return `
        ${topbar("Начальный экран", "Быстрый доступ к основным разделам мессенджера.")}
        <div class="grid dashboard-grid">
            ${statCard("Контакты", state.contacts.length, "Активные записи")}
            ${statCard("Чаты", state.chats.length, "Доступные диалоги")}
            ${statCard("Входящие", state.contactInvitationsIn.length + state.chatInvitationsIn.length, "Приглашения")}
            ${statCard("Исходящие", state.contactInvitationsOut.length + state.chatInvitationsOut.length, "Ожидают ответа")}
        </div>
        <div class="grid two-col" style="margin-top:16px">
            <section class="card">
                <div class="section-head"><h2 class="section-title">Входящие приглашения</h2></div>
                ${renderInvitationList("contact", state.contactInvitationsIn, true)}
                ${renderInvitationList("chat", state.chatInvitationsIn, true)}
            </section>
            <section class="card">
                <div class="section-head"><h2 class="section-title">Уведомления</h2></div>
                ${renderNotifications()}
            </section>
        </div>
        <section class="card" style="margin-top:16px">
            <div class="section-head"><h2 class="section-title">Быстрые переходы</h2></div>
            <div class="grid quick-actions">
                ${routes.filter(r => r.id !== "home").map(route => `
                    <button class="btn secondary" data-route="${route.id}">${route.icon} ${route.label}</button>
                `).join("")}
            </div>
        </section>
    `;
}

function renderNotifications() {
    if (!state.notifications.length) return `<div class="empty">Уведомлений пока нет.</div>`;
    return `<div class="list">${state.notifications.map(notification => `
        <article class="item">
            <div class="item-line">
                <div>
                    <div class="item-title">${escapeHtml(notification.title)}</div>
                    <div class="muted">${escapeHtml(notification.message)}</div>
                </div>
                <span class="pill">${fmtDate(notification.createdAt)}</span>
            </div>
        </article>
    `).join("")}</div>`;
}

function statCard(label, value, note) {
    return `
        <section class="card stat-card">
            <span class="muted">${label}</span>
            <strong>${value}</strong>
            <span>${note}</span>
        </section>
    `;
}

function renderContacts() {
    return `
        ${topbar("Контакты", "Книга контактов и приглашения на добавление.")}
        <div class="grid two-col">
            <section class="card">
                <h2 class="section-title">Добавить контакт</h2>
                <form class="form" data-form="contact-invite" style="margin-top:14px">
                    <label class="field"><span>ID пользователя</span><input class="input" name="receiverUserId" type="number" min="1" required></label>
                    <label class="field"><span>Сообщение</span><textarea class="textarea" name="message" placeholder="Здравствуйте, добавьте меня в контакты"></textarea></label>
                    <button class="btn" type="submit">Отправить приглашение</button>
                </form>
            </section>
            <section class="card">
                <div class="section-head"><h2 class="section-title">Мои контакты</h2><button class="btn secondary" data-refresh="contacts">Обновить</button></div>
                ${renderContactsList()}
            </section>
            <section class="card">
                <div class="section-head"><h2 class="section-title">Входящие</h2></div>
                ${renderInvitationList("contact", state.contactInvitationsIn, true)}
            </section>
            <section class="card">
                <div class="section-head"><h2 class="section-title">Исходящие</h2></div>
                ${renderInvitationList("contact", state.contactInvitationsOut, false)}
            </section>
        </div>
    `;
}

function renderContactsList() {
    if (!state.contacts.length) return `<div class="empty">Контактов пока нет.</div>`;
    return `<div class="list">${state.contacts.map(contact => `
        <article class="item">
            <div class="item-line">
                <div>
                    <div class="item-title">${escapeHtml(contact.displayName || contact.contact?.name || "Контакт")}</div>
                    <div class="muted">${escapeHtml(contact.contact?.login || `ID ${contact.contactUserId}`)}</div>
                </div>
                <span class="pill">с ${fmtDate(contact.createdAt)}</span>
            </div>
            <div>${escapeHtml(contact.contact?.email || "")} ${escapeHtml(contact.contact?.phone || "")}</div>
        </article>
    `).join("")}</div>`;
}

function renderInvitationList(kind, invitations, actionable) {
    if (!invitations.length) return `<div class="empty">Нет приглашений.</div>`;
    return `<div class="list">${invitations.map(invitation => {
        const title = kind === "contact"
            ? `${invitation.sender?.name || invitation.receiver?.name || "Пользователь"}`
            : `${invitation.chat?.name || "Чат"} · ${invitation.inviter?.name || "Пользователь"}`;
        return `
            <article class="item">
                <div class="item-line">
                    <div>
                        <div class="item-title">${escapeHtml(title)}</div>
                        <div class="muted">${escapeHtml(invitation.message || "Без сообщения")}</div>
                    </div>
                    <span class="pill warn">${escapeHtml(invitation.status)}</span>
                </div>
                <div class="muted">${fmtDate(invitation.createdAt)}</div>
                ${actionable ? `
                    <div class="button-row">
                        <button class="btn" data-invitation="${kind}" data-id="${invitation.id}" data-invitation-action="accept">Принять</button>
                        <button class="btn secondary" data-invitation="${kind}" data-id="${invitation.id}" data-invitation-action="reject">Отклонить</button>
                    </div>
                ` : `
                    <button class="btn secondary" data-invitation="${kind}" data-id="${invitation.id}" data-invitation-action="cancel">Отменить</button>
                `}
            </article>
        `;
    }).join("")}</div>`;
}

function renderChats() {
    const selected = state.chats.find(chat => chat.id === state.selectedChatId) || state.chats[0];
    const messages = selected ? (state.messages[selected.id] || []) : [];
    return `
        ${topbar("Чаты", "Список доступных чатов и приглашения участников.")}
        <div class="grid two-col" style="margin-bottom:16px">
            <section class="card">
                <h2 class="section-title">Создать чат</h2>
                <form class="form" data-form="chat-create" style="margin-top:14px">
                    <label class="field"><span>Название</span><input class="input" name="name" maxlength="120" required></label>
                    <button class="btn" type="submit">Создать</button>
                </form>
            </section>
            <section class="card">
                <h2 class="section-title">Пригласить в чат</h2>
                <form class="form" data-form="chat-invite" style="margin-top:14px">
                    <label class="field"><span>Чат</span>
                        <select class="input" name="idChat" required>
                            ${state.chats.map(chat => `<option value="${chat.id}">${escapeHtml(chat.name)}</option>`).join("")}
                        </select>
                    </label>
                    <label class="field"><span>ID пользователя</span><input class="input" name="inviteeUserId" type="number" min="1" required></label>
                    <label class="field"><span>Сообщение</span><textarea class="textarea" name="message"></textarea></label>
                    <button class="btn" type="submit" ${state.chats.length ? "" : "disabled"}>Отправить приглашение</button>
                </form>
            </section>
        </div>
        <section class="card" style="margin-bottom:16px">
            <div class="section-head"><h2 class="section-title">Входящие приглашения в чаты</h2></div>
            ${renderInvitationList("chat", state.chatInvitationsIn, true)}
        </section>
        <section class="chat-layout">
            <div class="chat-list">
                ${state.chats.length ? state.chats.map(chat => `
                    <button class="nav-btn ${selected?.id === chat.id ? "active" : ""}" data-chat-id="${chat.id}">
                        <span>◫</span><span>${escapeHtml(chat.name)}</span>
                    </button>
                `).join("") : `<div class="empty">Доступных чатов пока нет.</div>`}
            </div>
            <div class="chat-panel">
                <header class="chat-header">
                    <strong>${escapeHtml(selected?.name || "Выберите чат")}</strong>
                    <div class="muted">${selected ? `Владелец: ${escapeHtml(selected.ownerUser?.name || selected.owner)}` : "Выберите чат, чтобы увидеть сообщения"}</div>
                </header>
                <div class="messages">
                    ${selected ? renderMessages(messages) : `<div class="empty">Выберите чат.</div>`}
                </div>
                <div class="composer">
                    <form class="button-row composer-form" data-form="message" data-chat-id="${selected?.id || ""}">
                        <input class="input" name="value" placeholder="Сообщение" maxlength="4000" ${selected ? "" : "disabled"}>
                        <input class="input file-input" name="mediaFiles" type="file" multiple accept=".jpg,.jpeg,.png,.webp,.gif,.mp4,.webm,.mov,.pdf,.doc,.docx,.xls,.xlsx,.txt,.zip" ${selected ? "" : "disabled"}>
                        <button class="btn" type="submit" ${selected ? "" : "disabled"}>Отправить</button>
                    </form>
                </div>
            </div>
        </section>
    `;
}

function renderMessages(messages) {
    if (!messages.length) return `<div class="empty">Сообщений пока нет.</div>`;
    return messages.map(message => `
        <article class="bubble ${message.isMine ? "mine" : ""}">
            ${message.value ? `<div>${escapeHtml(message.value)}</div>` : ""}
            ${renderAttachments(message.attachments || [])}
            <div class="bubble-meta">${escapeHtml(message.sender?.name || `ID ${message.senderUserId}`)} · ${fmtDate(message.createdAt)}</div>
        </article>
    `).join("");
}

function renderAttachments(attachments) {
    if (!attachments.length) return "";

    return `<div class="attachments">${attachments.map(file => {
        const url = escapeHtml(mediaContentUrl(file.url));
        const name = escapeHtml(file.fileName);
        if (file.mediaType === "photo") {
            return `<a class="attachment-media" href="${url}" target="_blank" rel="noreferrer"><img src="${url}" alt="${name}"></a>`;
        }
        if (file.mediaType === "video") {
            return `<video class="attachment-media" src="${url}" controls preload="metadata"></video>`;
        }
        return `<a class="attachment-file" href="${url}" target="_blank" rel="noreferrer" download="${name}">${name}</a>`;
    }).join("")}</div>`;
}

function mediaContentUrl(url) {
    if (!state.token) return url;

    const separator = url.includes("?") ? "&" : "?";
    return `${url}${separator}token=${encodeURIComponent(state.token)}`;
}

function renderSettings() {
    return `
        ${topbar("Настройки", "Локальные параметры веб-интерфейса.")}
        <section class="card">
            <div class="settings-list">
                ${settingRow("Уведомления", "Показывать входящие приглашения на главном экране", "notifications")}
                ${settingRow("Компактный режим", "Уменьшить плотность списков в будущей версии", "compactMode")}
                ${settingRow("Звуки", "Звуковое сопровождение новых событий", "sound")}
            </div>
        </section>
    `;
}

function settingRow(title, description, key) {
    return `
        <div class="toggle-row">
            <div>
                <strong>${title}</strong>
                <div class="muted">${description}</div>
            </div>
            <button class="switch ${state.settings[key] ? "on" : ""}" data-setting="${key}" title="${title}"></button>
        </div>
    `;
}

function renderProfile() {
    return `
        ${topbar("Профиль", "Данные текущего пользователя из `/web/me`.")}
        <section class="card">
            <form class="form" data-form="profile">
                <label class="field"><span>Имя</span><input class="input" name="name" value="${escapeHtml(state.me?.name)}" required></label>
                <label class="field"><span>Email</span><input class="input" name="email" type="email" value="${escapeHtml(state.me?.email)}"></label>
                <label class="field"><span>Телефон</span><input class="input" name="phone" value="${escapeHtml(state.me?.phone)}"></label>
                <label class="field"><span>ФИО</span><input class="input" name="fio" value="${escapeHtml(state.me?.fio)}"></label>
                <div class="button-row">
                    <button class="btn" type="submit">Сохранить</button>
                    <span class="muted">Логин: ${escapeHtml(state.me?.login)}</span>
                </div>
            </form>
        </section>
    `;
}

function render() {
    if (!state.token) renderAuth();
    else renderApp();
}

app.addEventListener("submit", async (event) => {
    const form = event.target.closest("form");
    if (!form) return;
    event.preventDefault();

    const formType = form.dataset.form;
    if (formType === "login") await login(form);
    if (formType === "register") await register(form);
    if (formType === "profile") await updateProfile(form);
    if (formType === "contact-invite") await sendContactInvitation(form);
    if (formType === "chat-invite") await sendChatInvitation(form);
    if (formType === "chat-create") await createChat(form);
    if (formType === "message") await sendMessage(form);
});

app.addEventListener("click", async (event) => {
    const target = event.target.closest("button");
    if (!target) return;

    if (target.dataset.authMode) {
        state.authMode = target.dataset.authMode;
        state.message = "";
        render();
    }

    if (target.dataset.route) setRoute(target.dataset.route);
    if (target.dataset.action === "logout") logout();

    if (target.dataset.refresh === "contacts") {
        await loadContacts();
        render();
    }

    if (target.dataset.chatId) {
        state.selectedChatId = Number(target.dataset.chatId);
        await loadMessages(state.selectedChatId);
        render();
    }

    if (target.dataset.setting) {
        const key = target.dataset.setting;
        state.settings[key] = !state.settings[key];
        render();
    }

    if (target.dataset.invitation) {
        await invitationAction(
            target.dataset.invitation,
            Number(target.dataset.id),
            target.dataset.invitationAction
        );
    }
});

bootstrap();
