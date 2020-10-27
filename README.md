

## Общая информация
Каждый запрос должен содержать обязательные параметры:
- ```method``` - Название вызываемого метода. Значение должно иметь формат ```category.method```

Каждый возвращаемый запрос содержит параметры:
- ```result``` - Статус выполнения запроса
    * ```0``` - Запрос успешно выполнен
    * ```-1``` - Возникла ошибка при выполнении

## Авторизация и регистрация - ```auth```

<details>
  <summary><code>getAccessToken</code></summary>
  
  ---
  # auth.getAccessCode

  Получение кода для доступа к аккаунту.
  Код имеет срок годности, коотрый указывается в конфиге сервера.
  Большинство методов профиля требуют его.
  - **Запрос**
    * ```login``` - Логин/почта пользователя
    * ```password``` - Пароль пользователя

  - **Ответ**
    * ```access_token``` - Ключ доступа для аккаунта
    
  ---
  #### Запрос:

  ```yaml
  {
    "method":"auth.getAccessToken",
    "login":"login",
    "password":"pass"
  }
  ```
  #### Вывод:
  ```yaml
  {
    "result":"0",
    "access_token":"aaaaaaaaaaaaaaaaaaaa",
  }
  ```
  ---
</details>

<details>
  <summary><code>create</code></summary>
  
  ---
  # auth.create
  
  Создаёт аккаунт с заданным логином и паролем
  
  - **Запрос**
    * ```login``` - Логин/почта пользователя
    * ```password``` - Пароль пользователя

  - **Ответ**
    * ```result```
        - ```1``` - Неправильный формат логина
        - ```2``` - Данный логин уже занят
        - ```3``` - Неправильный формат пароля
        
  ---
  #### Запрос:

  ```yaml
  {
    "method":"auth.create",
    "login":"login",
    "password":"pass"
  }
  ```
  #### Вывод:
  ```yaml
  {
    "result":"0"
  }
  ```
  ---
</details>
   
## Профиль - ```profile```
Каждый запрос должен содержать параметр с ключом доступа: ```access_token```

<details>
  <summary><code>getData</code></summary>
  
  ---
  # profile.getData
  
  Возвращает данные об аккаунте
  
  - **Запрос**
    * ```fields``` - Значения, которые нужно узнать (перечисляются через запятую)
        - ```id``` - Уникальный идентификатор пользователя
        - ```has_skin``` - Показывает, имеет ли пользователь скин (1 - имеет, 0 - не имеет)
        - ```skin_url``` - URL ссылка на скин
        - ```login``` - Логин пользователя
        - ```email``` - Привязанная почта пользователя
        - ```status``` - Статус аккаунта

  - **Ответ**
    * ```data``` - содержит запрошенную информацию
    
  ---
  #### Запрос:

  ```yaml
  {
    "method":"auth.create",
    "key":"aaaaaaaaaaaaaaaaaaaa",
    "fields":"login,email"
  }
  ```
  #### Вывод:
  ```yaml
  {
    "result":"0",
    "data":{
      "login":"mylogin",
      "email":"mymail@mail.com"
    }
  }
  ```
  ---
</details>

<details>
  <summary><code>setData</code></summary>
  
  ---
  # profile.getData
  
  Изменяет данные аккаунта. При изменении почти необходим код из аккаунта.
  
  - **Запрос**
    * ```fields``` - Значения, которые нужно узнать (перечисляются через запятую)
        - ```id``` - Уникальный идентификатор пользователя
        - ```has_skin``` - Показывает, имеет ли пользователь скин (1 - имеет, 0 - не имеет)
        - ```skin_url``` - URL ссылка на скин
        - ```login``` - Логин пользователя
        - ```email``` - Привязанная почта пользователя (требует ```code```)
        - ```status``` - Статус аккаунта
    * ```code``` - Код подтверждения

  - **Ответ**
    * ```result```
        - ```1``` - Неправильный формат логина
        - ```2``` - Указанный логин уже существует
        - ```3``` - Неправильный формат почты
        - ```4``` - Неправильный код подтверждения
        - ```5``` - Неправильный формат пароля
        
  ---
  #### Запрос:

  ```yaml
  {
    "method":"auth.create",
    "key":"aaaaaaaaaaaaaaaaaaaa",
    "fields":"login,email"
    "code":"123456"
  }
  ```
  #### Вывод:
  ```yaml
  {
    "result":"0"
  }
  ```
  ---
  
</details>
        
<details>
  <summary><code>isEmailConfirmed</code></summary>
  
  - **Ответ**
    * ```result```
        - ```0``` - Почта подтверждена
        - ```1``` - Почта не подтверждена
  ---
</details>

<details>
  <summary><code>sendEmailCode</code></summary>
  
  - **Запрос**
    * ```email``` - Почта для подтверждения (если не указано, то берётся привязанная к аккаунту)

  - **Ответ**
    * ```result```
        - ```0``` - Код отправлен
        - ```1``` - Ошибка отправки кода
  ---
</details>

<details>
  <summary><code>confirmEmail</code></summary>
  
  - **Запрос**
    * ```email``` - Почта для подтверждения
    * ```code``` - Код подтверждения

  - **Ответ**
    * ```result```
        - ```3``` - Неправильный формат почты
        - ```4``` - Неправильный формат пароля
  ---
</details>
    
<details>
  <summary><code>getSkin</code></summary>

  - **Ответ**
    * ```skin``` - изображение скина в Base64
  ---
</details>

<details>
  <summary><code>setSkin</code></summary>
  
  - **Запрос**
    * ```skin``` - Изображение скина 64x64 в Base64
    * ```category``` - Категория скина
    * ```name``` - Название скина

  - **Ответ**
    * ```result```
        - ```1``` - Не указаны нужные параметры
        - ```2``` - Неправильный формат скина
  ---
</details>
    
## Профиль (Общее) - ```profiles```

<details>
  <summary><code>isLoginTaken</code></summary>
  
  - **Запрос**
    * ```login``` - Логин для проверки

  - **Ответ**
    * ```result```
        - ```0``` - Логин не используется
        - ```1``` - Логин уже используется
  ---
</details>

## Скины - ```skins```

<details>
  <summary><code>getCategories</code></summary>
  
  - **Ответ**
    * ```categories``` - список категорий скинов
  ---
</details>
    
<details>
  <summary><code>getCategoryPreview</code></summary>
  
  - **Запрос**
    * ```category``` - Категория скинов

  - **Ответ**
    * ```skin``` - Изображение скина в Base64
  ---
</details>
    
<details>
  <summary><code>getCategorySkinsList</code></summary>
  
  - **Запрос**
    * ```category``` - Категория скинов

  - **Ответ**
    * ```skins``` - Список названий скинов в категории
  ---
</details>

<details>
  <summary><code>getCategorySkin</code></summary>
  
  - **Запрос**
    * ```category``` - Категория скинов
    * ```name``` - Название скина

  - **Ответ**
    * ```skin``` - Изображение скина в Base64
  ---
</details>
    

