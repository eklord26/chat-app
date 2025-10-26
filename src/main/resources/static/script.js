document.addEventListener('DOMContentLoaded', function() {
    // --- Получаем ссылки на все элементы ---
    const userFilter = document.getElementById('userFilter');
    const eventTypeFilter = document.getElementById('eventTypeFilter');
    const eventNameFilter = document.getElementById('eventNameFilter');
    const ipOctetInputs = document.querySelectorAll('.ip-octet'); // Получаем все 4 поля IP
    const dateFromFilter = document.getElementById('dateFromFilter');
    const dateToFilter = document.getElementById('dateToFilter');
    const generateBtn = document.getElementById('generateReportBtn');
    const tableBody = document.getElementById('auditLogTableBody');

    // --- Логика для маски IP-адреса ---
    ipOctetInputs.forEach((input, index) => {
        input.addEventListener('input', () => {
            // 1. Оставляем только цифры
            input.value = input.value.replace(/[^0-9]/g, '');

            // 2. Проверяем, что значение не больше 255
            if (parseInt(input.value) > 255) {
                input.value = '255';
            }

            // 3. Автоматический переход к следующему полю
            if (input.value.length === 3 && index < ipOctetInputs.length - 1) {
                ipOctetInputs[index + 1].focus();
            }
        });

        input.addEventListener('keydown', (e) => {
            // 4. Переход к следующему полю по нажатию точки
            if (e.key === '.' || e.key === ' ') {
                e.preventDefault();
                if (index < ipOctetInputs.length - 1) {
                    ipOctetInputs[index + 1].focus();
                }
            }

            // 5. Переход к предыдущему полю по Backspace в пустом поле
            if (e.key === 'Backspace' && input.value.length === 0 && index > 0) {
                ipOctetInputs[index - 1].focus();
            }
        });
    });

    // --- Функция для загрузки пользователей в фильтр ---
    function populateUserFilter() {
        fetch('/users')
            .then(response => response.json())
            .then(users => {
                users.forEach(user => {
                    const option = document.createElement('option');
                    option.value = user.id;
                    option.textContent = `${user.name} (ID: ${user.id})`;
                    userFilter.appendChild(option);
                });
            })
            .catch(error => console.error('Ошибка при загрузке пользователей:', error));
    }

    // --- Функция для отображения данных в таблице ---
    function renderTable(data) {
        tableBody.innerHTML = ''; // Очищаем таблицу

        if (data.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="8" style="text-align: center;">Данные не найдены</td></tr>';
            return;
        }

        data.forEach(log => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${log.id}</td>
                <td>${log.name}</td>
                <td>${log.type}</td>
                <td>${log.userId}</td>
                <td>${log.timestamp}</td>
                <td>${log.description}</td>
                <td>${log.ipAddress}</td>
                <td>${log.ttl}</td>
            `;
            tableBody.appendChild(row);
        });
    }

    // --- Обработчик клика на кнопку "Сформировать" ---
    generateBtn.addEventListener('click', () => {
        const params = new URLSearchParams();

        // --- Сборка IP-адреса из полей ---
        const ipParts = Array.from(ipOctetInputs)
            .map(input => input.value)
            .filter(part => part !== ''); // Убираем пустые части

        if (ipParts.length > 0) {
            // Отправляем частичный IP, например "192.168."
            params.append('ipAddress', ipParts.join('.'));
        }

        // Собираем остальные параметры
        if (userFilter.value) params.append('userId', userFilter.value);
        if (eventTypeFilter.value) params.append('eventType', eventTypeFilter.value);
        if (eventNameFilter.value) params.append('eventName', eventNameFilter.value);
        if (dateFromFilter.value) params.append('dateFrom', dateFromFilter.value);
        if (dateToFilter.value) params.append('dateTo', dateToFilter.value);

        const url = `/api/audit-logs?${params.toString()}`;

        // Выполняем запрос
        fetch(url)
            .then(response => response.json())
            .then(data => renderTable(data))
            .catch(error => {
                console.error('Ошибка при получении логов:', error);
                tableBody.innerHTML = '<tr><td colspan="8" style="text-align: center; color: red;">Не удалось загрузить данные.</td></tr>';
            });
    });

    // --- Инициализация ---
    populateUserFilter();
});