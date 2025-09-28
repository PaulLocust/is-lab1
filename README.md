## 🚀 Как запускать

```bash
# 1. Клонируйте репозиторий
git clone https://github.com/PaulLocust/is-lab1.git

# 2. Настройте окружение и введите свои данные
mv .env.example .env

# 3. Убедитесь в том, что в .env 
SPRING_PROFILES_ACTIVE=prod

# 4. Соберите jar файл приложения
gradle build

# 5. Перенесите свои .env и app.jar в один репозиторий на гелиосе, например, с помощью SFTP

# 6.  Пробросьте порты с локальной машины на гелиос
ssh -p 2222 -L 28000:localhost:28000 s409517@helios.cs.ifmo.ru

# 7. Зайдите в директорию с .env и app.jar и запустите программу
java -jar app.jar
```

## После запуска доступны:
- 📚 http://localhost:28000/swagger-ui.html API документация

- 📊 http://localhost:28000/ Основная страница сайта
