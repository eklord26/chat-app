const app = document.getElementById("app");

const state = {
    route: "chats",
    authMode: "login",
    token: localStorage.getItem("authToken"),
    userId: Number(localStorage.getItem("userId")) || null,
    me: null,
    contacts: [],
    chats: [],
    roles: [],
    auditLogs: [],
    messages: {},
    selectedChatId: Number(localStorage.getItem("lastChatId")) || null,
    focusMessageId: null,
    createChatPrefillContactId: null,
    viewedUser: null,
    notifications: JSON.parse(localStorage.getItem("socketNotifications") || "[]"),
    notificationsOpen: false,
    invitationNotification: null,
    message: "",
    messageType: "error",
    loading: false
};

let socket = null;
let socketReconnectTimer = null;

const navItems = [
    { id: "chats", label: "Чаты" },
    { id: "contacts", label: "Контакты" },
    { id: "profile", label: "Профиль" },
    { id: "create-chat", label: "Создать новый чат" }
];

function visibleNavItems() {
    return state.me?.isAdmin
        ? [...navItems, { id: "administration", label: "Администрирование" }]
        : navItems;
}

async function api(path, options = {}) {
    const authRequired = options.authRequired !== false;
    const sendAuth = options.sendAuth !== false;
    const requestOptions = { ...options };
    delete requestOptions.authRequired;
    delete requestOptions.sendAuth;

    const headers = { ...(requestOptions.headers || {}) };
    if (!(requestOptions.body instanceof FormData)) {
        headers["Content-Type"] = "application/json";
    }
    if (sendAuth && state.token) {
        headers.Authorization = `Bearer ${state.token}`;
    }

    const response = await fetch(path, { ...requestOptions, headers });
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
    if (!value) return "";
    const date = new Date(value);
    return Number.isNaN(date.getTime())
        ? value
        : date.toLocaleString("ru-RU", { dateStyle: "short", timeStyle: "short" });
}

function setMessage(text, type = "error") {
    state.message = text;
    state.messageType = type;
    render();
}

async function loadDesignSettings() {
    try {
        const colors = await api("/web/design-settings", { authRequired: false, sendAuth: false });
        const root = document.documentElement;
        Object.entries({
            primary: "--primary",
            secondary: "--secondary",
            warning: "--warning",
            error: "--error",
            success: "--success"
        }).forEach(([key, cssVar]) => {
            if (colors[key]) root.style.setProperty(cssVar, colors[key]);
        });
        if (colors.error) root.style.setProperty("--danger", colors.error);
    } catch {
        // Defaults from CSS remain active.
    }
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
        localStorage.setItem("userId", String(state.userId));
        await loadAppData();
        if (state.selectedChatId) {
            await loadMessages(state.selectedChatId);
            await loadAppData();
        }
        connectSocket();
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

async function loadAppData() {
    const [contacts, chats, roles] = await Promise.all([
        api("/web/contacts").catch(() => []),
        api("/web/chats").catch(() => []),
        api("/web/chat-roles").catch(() => [])
    ]);
    state.contacts = contacts;
    state.chats = chats;
    state.roles = roles;
}

async function loadMessages(chatId) {
    state.messages[chatId] = await api(`/web/chats/${chatId}/messages`).catch(() => []);
}

async function loadAuditLogs() {
    if (!state.me?.isAdmin) {
        state.auditLogs = [];
        return;
    }
    state.auditLogs = await api("/web/admin/audit-logs").catch(() => []);
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
        state.route = "chats";
        state.message = "";
        localStorage.setItem("authToken", result.authToken);
        localStorage.setItem("userId", String(result.id));
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
        state.route = "chats";
        state.message = "";
        localStorage.setItem("authToken", result.authToken);
        localStorage.setItem("userId", String(result.id));
        await bootstrap();
    } catch (error) {
        state.loading = false;
        setMessage(error.message);
    }
}

function logout(renderAfter = true) {
    closeSocket();
    state.token = null;
    state.userId = null;
    state.me = null;
    state.selectedChatId = null;
    state.notifications = [];
    state.notificationsOpen = false;
    state.invitationNotification = null;
    localStorage.removeItem("authToken");
    localStorage.removeItem("userId");
    localStorage.removeItem("lastChatId");
    localStorage.removeItem("socketNotifications");
    if (renderAfter) render();
}

async function updateProfile(form) {
    const body = Object.fromEntries(new FormData(form).entries());
    try {
        state.me = await api("/web/me", {
            method: "PUT",
            body: JSON.stringify({
                name: body.name,
                fio: body.fio,
                email: state.me?.email || null,
                phone: state.me?.phone || null
            })
        });
        setMessage("Профиль обновлен", "success");
    } catch (error) {
        setMessage(error.message);
    }
}

async function changePassword(form) {
    const body = Object.fromEntries(new FormData(form).entries());
    if (body.newPassword !== body.repeatPassword) {
        setMessage("Новый пароль и повтор пароля не совпадают.");
        return;
    }

    try {
        await api("/web/me/password", {
            method: "PUT",
            body: JSON.stringify({
                currentPassword: body.currentPassword,
                newPassword: body.newPassword
            })
        });
        form.reset();
        setMessage("Пароль изменен", "success");
    } catch (error) {
        setMessage(error.message);
    }
}

async function deleteContact(contactId) {
    if (!confirm("Удалить контакт?")) return;
    try {
        await api(`/web/contacts/${contactId}`, { method: "DELETE" });
        await loadAppData();
        setMessage("Контакт удален", "success");
    } catch (error) {
        setMessage(error.message);
    }
}

async function viewContact(userId) {
    try {
        state.viewedUser = await api(`/web/users/${userId}/public`);
        render();
    } catch (error) {
        setMessage(error.message);
    }
}

async function writeContact(contact) {
    try {
        const chat = await api(`/web/chats/direct/${contact.contactUserId}`);
        await openChat(chat.id);
    } catch (error) {
        if (error.status !== 404) {
            setMessage(error.message);
            return;
        }
        state.createChatPrefillContactId = contact.contactUserId;
        state.route = "create-chat";
        render();
    }
}

async function createChat(form) {
    const body = Object.fromEntries(new FormData(form).entries());
    const selectedContactIds = Array.from(form.querySelectorAll("input[name='contactUserId']:checked"))
        .map(input => Number(input.value));
    const name = body.name?.trim();
    if (!name) {
        setMessage("Укажите название чата.");
        return;
    }

    try {
        const chat = await api("/web/chats", {
            method: "POST",
            body: JSON.stringify({ name })
        });

        const administratorRole = state.roles.find(role => role.name === "Администратор");
        const selectedRole = state.roles.find(role => role.id === Number(body.idRole));
        const roleForInvites = selectedContactIds.length === 1
            ? administratorRole
            : selectedRole;

        await Promise.all(selectedContactIds.map(contactUserId => api("/web/chat-invitations", {
            method: "POST",
            body: JSON.stringify({
                idChat: chat.id,
                inviteeUserId: contactUserId,
                idRole: roleForInvites?.id || null,
                message: body.message || null
            })
        })));

        state.createChatPrefillContactId = null;
        await loadAppData();
        await openChat(chat.id);
        setMessage("Чат создан", "success");
    } catch (error) {
        setMessage(error.message);
    }
}

async function openChat(chatId, focusMessageId = null) {
    state.route = "chats";
    state.selectedChatId = Number(chatId);
    state.focusMessageId = focusMessageId ? Number(focusMessageId) : null;
    localStorage.setItem("lastChatId", String(chatId));
    await loadMessages(chatId);
    await loadAppData();
    render();
}

async function sendMessage(form) {
    const chatId = Number(form.dataset.chatId);
    const body = Object.fromEntries(new FormData(form).entries());
    const files = Array.from(form.elements.mediaFiles?.files || []);
    const value = body.value?.trim();
    if (!value && files.length === 0) return;

    try {
        const mediaFiles = files.length ? await uploadMediaFiles(files) : [];
        await api(`/web/chats/${chatId}/messages`, {
            method: "POST",
            body: JSON.stringify({
                value: value || "",
                type: "text",
                mediaFileIds: mediaFiles.map(file => file.id)
            })
        });
        form.reset();
        await loadMessages(chatId);
        await loadAppData();
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

async function setRoute(route) {
    if (route === "administration" && !state.me?.isAdmin) return;
    state.route = route;
    state.message = "";
    if (route !== "create-chat") state.createChatPrefillContactId = null;
    if (route === "administration") await loadAuditLogs();
    render();
}

function connectSocket() {
    if (!state.token || socket?.readyState === WebSocket.OPEN || socket?.readyState === WebSocket.CONNECTING) return;

    clearTimeout(socketReconnectTimer);
    const protocol = window.location.protocol === "https:" ? "wss:" : "ws:";
    socket = new WebSocket(`${protocol}//${window.location.host}/ws?token=${encodeURIComponent(state.token)}`);

    socket.addEventListener("message", (event) => {
        handleSocketEvent(event.data);
    });

    socket.addEventListener("close", () => {
        socket = null;
        if (state.token) {
            socketReconnectTimer = setTimeout(connectSocket, 3000);
        }
    });

    socket.addEventListener("error", () => {
        socket?.close();
    });
}

function closeSocket() {
    clearTimeout(socketReconnectTimer);
    socketReconnectTimer = null;
    if (socket) {
        socket.onclose = null;
        socket.close();
        socket = null;
    }
}

async function handleSocketEvent(rawEvent) {
    const event = safeJson(rawEvent);
    if (!event || !event.data) return;

    if (event.event === "invitation:created") {
        handleInvitationCreated(event.data);
        return;
    }

    if (event.event === "invitation:updated") {
        await handleInvitationUpdated(event.data);
        return;
    }

    if (event.event !== "message:new") return;

    const message = event.data;
    const chatId = Number(message.idChat);
    if (!chatId) return;
    const isMine = Number(message.senderUserId) === state.userId;

    if (!isMine) addNotification({
        type: "message",
        title: "Новое сообщение",
        message: `${message.sender?.name || "Пользователь"}: ${message.value || "Медиафайл"}`,
        chatId,
        messageId: message.id,
        createdAt: message.createdAt
    });

    const isOpenedChat = state.route === "chats" && state.selectedChatId === chatId;
    if (isOpenedChat) {
        await loadMessages(chatId);
        await loadAppData();
        render();
        return;
    }

    const cachedMessages = state.messages[chatId];
    if (cachedMessages && !cachedMessages.some(item => item.id === message.id)) {
        cachedMessages.push(normalizeSocketMessage(message));
    }
    await loadAppData();
    if (state.route === "chats") render();
}

function handleInvitationCreated(data) {
    if (data.direction !== "incoming" || !data.invitation) return;
    const invitation = data.invitation;
    const isChat = data.kind === "chat";
    const sender = isChat ? invitation.inviter : invitation.sender;
    const sourceName = isChat ? invitation.chat?.name : sender?.name;

    addNotification({
        type: "invitation",
        kind: data.kind,
        title: isChat ? "Приглашение в чат" : "Запрос в контакты",
        message: sourceName || "Новое приглашение",
        invitation,
        createdAt: invitation.createdAt
    });
}

async function handleInvitationUpdated(data) {
    const invitation = data.invitation;
    if (!invitation) return;
    if (invitation.status && invitation.status !== "pending") {
        removeNotification(notification =>
            notification.type === "invitation" &&
            notification.kind === data.kind &&
            notification.invitation?.id === invitation.id
        );
    }
    await loadAppData();
    if (state.route === "chats" || state.route === "contacts") render();
}

function addNotification(notification) {
    state.notifications.push({
        id: `${Date.now()}-${Math.random().toString(16).slice(2)}`,
        createdAt: new Date().toISOString(),
        ...notification
    });
    persistNotifications();
    render();
}

function removeNotification(predicate) {
    state.notifications = state.notifications.filter(notification => !predicate(notification));
    persistNotifications();
}

function persistNotifications() {
    localStorage.setItem("socketNotifications", JSON.stringify(state.notifications));
}

function normalizeSocketMessage(message) {
    const isMine = Number(message.senderUserId) === state.userId;
    return {
        ...message,
        isMine,
        isUnread: !isMine && message.viewedAt == null
    };
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
                    <button class="btn ghost" data-auth-mode="${isLogin ? "register" : "login"}">${isLogin ? "Создать аккаунт" : "Уже есть аккаунт"}</button>
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
        <label class="field"><span>Имя</span><input class="input" name="username" required></label>
        <label class="field"><span>ФИО</span><input class="input" name="fio" required></label>
        <label class="field"><span>Email</span><input class="input" name="email" type="email" required></label>
        <label class="field"><span>Телефон</span><input class="input" name="phone" placeholder="+79991234567" required></label>
        <label class="field"><span>Логин</span><input class="input" name="login" autocomplete="username" required></label>
        <label class="field"><span>Пароль</span><input class="input" name="password" type="password" autocomplete="new-password" required></label>
    `;
}

function renderApp() {
    app.innerHTML = `
        <main class="messenger-layout">
            <aside class="app-sidebar">
                <div class="sidebar-brand-row">
                    <div class="brand-lockup">
                        <img class="brand-logo" src="/static/logo.png" alt="Connect">
                        <span>Connect</span>
                    </div>
                    <button class="notification-button" data-action="toggle-notifications" title="Уведомления">
                        <span aria-hidden="true">🔔</span>
                        ${state.notifications.length ? `<span class="notification-count">${state.notifications.length}</span>` : ""}
                    </button>
                </div>
                ${state.notificationsOpen ? renderNotificationsPanel() : ""}
                <nav class="sidebar-nav">
                    ${visibleNavItems().map(item => `
                        <button class="nav-btn ${state.route === item.id ? "active" : ""}" data-route="${item.id}">${item.label}</button>
                    `).join("")}
                </nav>
                <button class="btn danger sidebar-logout" data-action="logout">Выйти</button>
            </aside>
            <section class="workspace">
                ${renderView()}
                ${state.message ? `<p class="message ${state.messageType === "success" ? "success" : ""}">${escapeHtml(state.message)}</p>` : ""}
            </section>
            ${state.viewedUser ? renderUserModal() : ""}
            ${state.invitationNotification ? renderInvitationModal() : ""}
        </main>
    `;
    focusSelectedChatMessages();
}

function renderView() {
    if (state.route === "contacts") return renderContacts();
    if (state.route === "profile") return renderProfile();
    if (state.route === "create-chat") return renderCreateChat();
    if (state.route === "administration" && state.me?.isAdmin) return renderAdministration();
    return renderChats();
}

function renderNotificationsPanel() {
    return `
        <section class="notifications-panel">
            ${state.notifications.length ? state.notifications.map(notification => `
                <button class="notification-item" data-notification-id="${notification.id}">
                    <strong>${escapeHtml(notification.title)}</strong>
                    <span>${escapeHtml(notification.message)}</span>
                    <small>${fmtDate(notification.createdAt)}</small>
                </button>
            `).join("") : `<div class="empty">Уведомлений пока нет.</div>`}
        </section>
    `;
}

function renderAdministration() {
    return `
        <div class="view-title">
            <h1>Администрирование</h1>
            <p>Журнал аудита системных событий.</p>
        </div>
        <section class="admin-audit">
            <div class="section-head">
                <h2 class="section-title">Аудит</h2>
                <button class="btn secondary" data-action="refresh-audit">Обновить</button>
            </div>
            ${state.auditLogs.length ? `
                <div class="audit-table">
                    <div class="audit-row audit-head">
                        <span>Дата</span>
                        <span>Тип</span>
                        <span>Событие</span>
                        <span>Пользователь</span>
                        <span>IP</span>
                        <span>Описание</span>
                    </div>
                    ${state.auditLogs.map(log => `
                        <article class="audit-row">
                            <span>${fmtDate(log.date)}</span>
                            <span><mark class="audit-type">${escapeHtml(log.type)}</mark></span>
                            <span>${escapeHtml(log.event)}</span>
                            <span>ID ${escapeHtml(log.userId)}</span>
                            <span>${escapeHtml(log.ipAddress)}</span>
                            <span>${escapeHtml(log.description)}</span>
                        </article>
                    `).join("")}
                </div>
            ` : `<div class="empty">Записей аудита пока нет.</div>`}
        </section>
    `;
}

function renderChats() {
    const selected = state.chats.find(chat => chat.id === state.selectedChatId);
    const messages = selected ? (state.messages[selected.id] || []) : [];
    return `
        <section class="chat-workspace">
            <aside class="chat-list-pane">
                <div class="section-head">
                    <h1 class="section-title">Чаты</h1>
                    <button class="btn icon" title="Создать чат" data-route="create-chat">+</button>
                </div>
                <div class="chat-list">
                    ${state.chats.length ? state.chats.map(chat => `
                        <button class="chat-list-item ${selected?.id === chat.id ? "active" : ""}" data-chat-id="${chat.id}">
                            <span class="chat-list-item-title">
                                <strong>${escapeHtml(chat.name)}</strong>
                                ${chat.unreadCount ? `<span class="chat-unread-badge">${chat.unreadCount}</span>` : ""}
                            </span>
                            <span>${escapeHtml(chat.ownerUser?.name || "Чат")}</span>
                        </button>
                    `).join("") : `<div class="empty">Чатов пока нет.</div>`}
                </div>
            </aside>
            <section class="chat-conversation">
                ${selected ? `
                    <header class="chat-header">
                        <strong>${escapeHtml(selected.name)}</strong>
                    </header>
                    ${renderChatParticipants(selected)}
                    <div class="messages">${renderMessages(messages)}</div>
                    <form class="composer composer-form" data-form="message" data-chat-id="${selected.id}">
                        <div class="selected-media-preview" data-preview-for="${selected.id}"></div>
                        <div class="composer-controls">
                            <input class="input" name="value" placeholder="Сообщение" maxlength="4000">
                            <label class="attach-button" title="Прикрепить файл">
                                <span aria-hidden="true">📎</span>
                                <span class="attach-count" data-attach-count hidden>0</span>
                                <input name="mediaFiles" type="file" multiple accept="image/*,video/*,.pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.txt,.csv,.rtf,.odt,.ods,.odp,.zip,.rar,.7z">
                            </label>
                            <button class="btn" type="submit">Отправить</button>
                        </div>
                    </form>
                ` : `
                    <div class="empty conversation-empty">Выберите чат для начала общения</div>
                `}
            </section>
        </section>
    `;
}

function renderMessages(messages) {
    if (!messages.length) return `<div class="empty">Сообщений пока нет.</div>`;
    return messages.map(message => {
        const attachments = message.attachments || [];
        const hasAttachments = Boolean(attachments.length);
        const hasFileAttachments = attachments.some(file => file.mediaType !== "photo" && file.mediaType !== "video");
        return `
        <article class="bubble ${message.isMine ? "mine" : ""} ${message.isUnread ? "unread" : ""} ${hasAttachments ? "with-attachments" : ""} ${hasFileAttachments ? "with-files" : ""}" data-message-id="${message.id}" data-unread="${message.isUnread ? "true" : "false"}">
            ${message.value ? `<div>${escapeHtml(message.value)}</div>` : ""}
            ${renderAttachments(attachments)}
            <div class="bubble-meta">${escapeHtml(message.sender?.name || `ID ${message.senderUserId}`)} · ${fmtDate(message.createdAt)}</div>
        </article>
    `;
    }).join("");
}

function participantNames(chat) {
    return (chat.participants || [])
        .map(user => user.name || user.login || `ID ${user.id}`)
        .filter(Boolean);
}

function renderChatParticipants(chat) {
    const participants = participantNames(chat);
    if (!participants.length) return "";
    const visibleParticipants = participants.slice(0, 5);
    const hiddenCount = Math.max(participants.length - visibleParticipants.length, 0);

    return `
        <div class="chat-participants-panel">
            <span>Участники</span>
            <div class="chat-participant-list">
                ${visibleParticipants.map(name => `<span class="chat-participant-chip">${escapeHtml(name)}</span>`).join("")}
                ${hiddenCount ? `<span class="chat-participant-chip">+${hiddenCount}</span>` : ""}
            </div>
        </div>
    `;
}

function focusSelectedChatMessages() {
    if (state.route !== "chats" || !state.selectedChatId) return;

    requestAnimationFrame(() => {
        const messages = app.querySelector(".chat-conversation .messages");
        if (!messages) return;

        const focused = state.focusMessageId
            ? messages.querySelector(`[data-message-id="${state.focusMessageId}"]`)
            : null;
        const unread = messages.querySelector('[data-unread="true"]');
        const target = focused || unread || messages.lastElementChild;
        if (!target) return;

        target.scrollIntoView({ block: focused || unread ? "center" : "end" });
        if (focused) {
            target.classList.add("focused");
            setTimeout(() => target.classList.remove("focused"), 1800);
            state.focusMessageId = null;
        }
    });
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

function renderContacts() {
    return `
        <div class="view-title">
            <h1>Контакты</h1>
            <p>Список всех контактов пользователя.</p>
        </div>
        <section class="card">
            ${state.contacts.length ? `
                <div class="list">
                    ${state.contacts.map(contact => `
                        <article class="contact-row">
                            <div>
                                <strong>${escapeHtml(contact.contact?.name || "Контакт")}</strong>
                                <div class="muted">${escapeHtml(contact.contact?.login || `ID ${contact.contactUserId}`)}</div>
                            </div>
                            <div class="button-row">
                                <button class="btn secondary" data-contact-action="view" data-user-id="${contact.contactUserId}">Посмотреть</button>
                                <button class="btn" data-contact-action="write" data-contact-id="${contact.id}">Написать</button>
                                <button class="btn danger" data-contact-action="delete" data-contact-id="${contact.id}">Удалить</button>
                            </div>
                        </article>
                    `).join("")}
                </div>
            ` : `<div class="empty">Контактов пока нет.</div>`}
        </section>
    `;
}

function renderProfile() {
    return `
        <div class="view-title">
            <h1>Профиль</h1>
            <p>Изменение публичных данных и пароля.</p>
        </div>
        <div class="grid two-col">
            <section class="card">
                <h2 class="section-title">Данные профиля</h2>
                <form class="form" data-form="profile" style="margin-top:14px">
                    <label class="field"><span>Никнейм</span><input class="input" name="name" value="${escapeHtml(state.me?.name)}" required></label>
                    <label class="field"><span>ФИО</span><input class="input" name="fio" value="${escapeHtml(state.me?.fio)}"></label>
                    <button class="btn" type="submit">Сохранить профиль</button>
                </form>
            </section>
            <section class="card">
                <h2 class="section-title">Смена пароля</h2>
                <form class="form" data-form="password" style="margin-top:14px">
                    <label class="field"><span>Текущий пароль</span><input class="input" name="currentPassword" type="password" required></label>
                    <label class="field"><span>Новый пароль</span><input class="input" name="newPassword" type="password" minlength="8" required></label>
                    <label class="field"><span>Повторите новый пароль</span><input class="input" name="repeatPassword" type="password" minlength="8" required></label>
                    <button class="btn" type="submit">Изменить пароль</button>
                </form>
            </section>
        </div>
    `;
}

function renderCreateChat() {
    const prefillId = state.createChatPrefillContactId;
    const participantRole = state.roles.find(role => role.name === "Участник");
    return `
        <div class="view-title">
            <h1>Создание чата</h1>
            <p>Укажите название, выберите контакты и настройте приглашение.</p>
        </div>
        <section class="card create-chat-panel">
            <form class="form" data-form="chat-create">
                <label class="field"><span>Название чата</span><input class="input" name="name" maxlength="120" required></label>
                <div class="field">
                    <span>Кого пригласить</span>
                    <div class="checkbox-list">
                        ${state.contacts.length ? state.contacts.map(contact => `
                            <label class="check-row">
                                <input type="checkbox" name="contactUserId" value="${contact.contactUserId}" ${prefillId === contact.contactUserId ? "checked" : ""}>
                                <span>${escapeHtml(contact.contact?.name || contact.contact?.login || `ID ${contact.contactUserId}`)}</span>
                            </label>
                        `).join("") : `<div class="empty">Контактов пока нет.</div>`}
                    </div>
                </div>
                <label class="field"><span>Сообщение приглашения</span><textarea class="textarea" name="message"></textarea></label>
                <label class="field"><span>Роль пользователя в чате</span>
                    <select class="input" name="idRole">
                        ${state.roles.map(role => `<option value="${role.id}" ${role.id === participantRole?.id ? "selected" : ""}>${escapeHtml(role.name)}</option>`).join("")}
                    </select>
                </label>
                <p class="muted">Если выбран только один контакт, приглашенному автоматически будет выдана роль Администратор.</p>
                <button class="btn" type="submit">Создать чат</button>
            </form>
        </section>
    `;
}

function renderUserModal() {
    return `
        <div class="modal-backdrop">
            <section class="modal">
                <div class="section-head">
                    <h2 class="section-title">Публичная информация</h2>
                    <button class="btn secondary" data-action="close-user-modal">Закрыть</button>
                </div>
                <div class="list">
                    <div class="item"><strong>Имя</strong><span>${escapeHtml(state.viewedUser.name)}</span></div>
                    <div class="item"><strong>Ник</strong><span>${escapeHtml(state.viewedUser.login)}</span></div>
                </div>
            </section>
        </div>
    `;
}

function renderInvitationModal() {
    const notification = state.invitationNotification;
    const invitation = notification.invitation;
    const isChat = notification.kind === "chat";
    const title = isChat ? "Приглашение в чат" : "Запрос в контакты";
    const source = isChat
        ? invitation.chat?.name
        : invitation.sender?.name || invitation.sender?.login;

    return `
        <div class="modal-backdrop">
            <section class="modal">
                <div class="section-head">
                    <h2 class="section-title">${title}</h2>
                    <button class="btn secondary" data-action="close-invitation-modal">Закрыть</button>
                </div>
                <div class="list">
                    <div class="item"><strong>${escapeHtml(source || "Приглашение")}</strong><span>${escapeHtml(invitation.message || "")}</span></div>
                </div>
                <div class="button-row" style="margin-top:16px">
                    <button class="btn" data-action="accept-invitation" data-notification-id="${notification.id}">Принять</button>
                    <button class="btn danger" data-action="reject-invitation" data-notification-id="${notification.id}">Отклонить</button>
                </div>
            </section>
        </div>
    `;
}

async function openNotification(notificationId) {
    const notification = state.notifications.find(item => item.id === notificationId);
    if (!notification) return;

    state.notificationsOpen = false;
    if (notification.type === "message" && notification.chatId) {
        removeNotification(item => item.id === notificationId);
        await openChat(notification.chatId, notification.messageId);
        return;
    }

    if (notification.type === "invitation") {
        state.invitationNotification = notification;
        render();
    }
}

async function respondInvitation(notificationId, action) {
    const notification = state.notifications.find(item => item.id === notificationId);
    if (!notification?.invitation) return;

    const endpointRoot = notification.kind === "chat" ? "/web/chat-invitations" : "/web/contact-invitations";
    try {
        await api(`${endpointRoot}/${notification.invitation.id}/${action}`, { method: "POST" });
        removeNotification(item => item.id === notificationId);
        state.invitationNotification = null;
        await loadAppData();
        render();
    } catch (error) {
        setMessage(error.message);
    }
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
    if (formType === "password") await changePassword(form);
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
    if (target.dataset.route) await setRoute(target.dataset.route);
    if (target.dataset.action === "logout") logout();
    if (target.dataset.action === "refresh-audit") {
        await loadAuditLogs();
        render();
    }
    if (target.dataset.action === "toggle-notifications") {
        state.notificationsOpen = !state.notificationsOpen;
        render();
    }
    if (target.dataset.action === "close-user-modal") {
        state.viewedUser = null;
        render();
    }
    if (target.dataset.action === "close-invitation-modal") {
        state.invitationNotification = null;
        render();
    }
    if (target.dataset.action === "accept-invitation") await respondInvitation(target.dataset.notificationId, "accept");
    if (target.dataset.action === "reject-invitation") await respondInvitation(target.dataset.notificationId, "reject");
    if (target.dataset.notificationId && !target.dataset.action) await openNotification(target.dataset.notificationId);
    if (target.dataset.chatId) await openChat(Number(target.dataset.chatId));

    if (target.dataset.contactAction) {
        const action = target.dataset.contactAction;
        const contactId = Number(target.dataset.contactId);
        const contact = state.contacts.find(item => item.id === contactId);
        if (action === "delete") await deleteContact(contactId);
        if (action === "write" && contact) await writeContact(contact);
        if (action === "view") await viewContact(Number(target.dataset.userId));
    }
});

app.addEventListener("change", (event) => {
    const input = event.target;
    if (!(input instanceof HTMLInputElement) || input.name !== "mediaFiles") return;

    const form = input.closest("form");
    const preview = form?.querySelector(".selected-media-preview");
    const counter = form?.querySelector("[data-attach-count]");
    if (!preview) return;

    const files = Array.from(input.files || []);
    updateAttachCounter(counter, files.length);
    preview.innerHTML = files.map(file => {
        const name = escapeHtml(file.name);
        if (file.type.startsWith("image/")) {
            const url = URL.createObjectURL(file);
            return `<span class="selected-media-item"><img src="${url}" alt="${name}"><span>${name}</span></span>`;
        }

        return `<span class="selected-media-item"><span class="selected-file-icon">📎</span><span>${name}</span></span>`;
    }).join("");
});

function updateAttachCounter(counter, count) {
    if (!counter) return;
    counter.textContent = String(count);
    counter.hidden = count === 0;
}

bootstrap();
