

## Общая информация
Каждый запрос должен содержать обязательные параметры:
- ```method``` - Название вызываемого метода. Значение имеет формат: ```category.method```

Каждый возвращаемый запрос содержит параметры:
- ```result``` - Статус выполнения запроса
    * ```0``` - Запрос успешно выполнен
    * ```-1``` - Возникла ошибка при выполнении

## Авторизация и регистрация

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
        - ```0``` - Аккаунт был создан
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
   
## Профиль
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
    "method":"profile.getData",
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
  # profile.setData
  
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
        - ```0``` - Все данные были успешно изменены
        - ```1``` - Неправильный формат логина
        - ```2``` - Указанный логин уже существует
        - ```3``` - Неправильный формат почты
        - ```4``` - Неправильный код подтверждения
        - ```5``` - Неправильный формат пароля
        
  ---
  #### Запрос:

  ```yaml
  {
    "method":"profile.setData",
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
   
   ---
  # profile.isEmailConfirmed
  
  Показывает, подтверждена ли почта у аккаунта
  
  - **Ответ**
    * ```result```
        - ```0``` - Почта подтверждена
        - ```1``` - Почта не подтверждена
  ---
  #### Запрос:

  ```yaml
  {
    "method":"profile.isEmailConfirmed",
    "key":"aaaaaaaaaaaaaaaaaaaa",
  }
  ```
  #### Вывод:
  ```yaml
  {
    "result":"1"
  }
  ```
  ---
</details>

<details>
  <summary><code>sendEmailCode</code></summary>
   
  ---
  # profile.sendEmailCode
  
  Отправляет код подтверждения на указанную почту
  
  - **Запрос**
    * ```email``` - Почта для подтверждения (если не указано, то берётся привязанная к аккаунту)

  - **Ответ**
    * ```result```
        - ```0``` - Код отправлен
        - ```1``` - Ошибка отправки кода
  ---
  #### Запрос:

  ```yaml
  {
    "method":"profile.sendEmailCode",
    "key":"aaaaaaaaaaaaaaaaaaaa",
    "email":"mymail@mail.com"
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
  <summary><code>confirmEmail</code></summary>
   
  ---
  # profile.confirmEmail
  
  Подтверждает почту, проверяя переданный код
  
  - **Запрос**
    * ```email``` - Почта для подтверждения
    * ```code``` - Код подтверждения

  - **Ответ**
    * ```result```
        - ```0``` - Почта была подтверждена
        - ```3``` - Неправильный формат почты
        - ```4``` - Неправильный формат пароля
  ---
  #### Запрос:

  ```yaml
  {
    "method":"profile.confirmEmail",
    "key":"aaaaaaaaaaaaaaaaaaaa",
    "email":"mymail@mail.com"
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
  <summary><code>getSkin</code></summary>
   
  ---
  # profile.getSkin
  
  Возвращает установленный скин у аккаунта

  - **Ответ**
    * ```result```
      - ```0``` - Скин был успешно установлен
      - ```1``` - Аккаунт не имеет установленного скина
    * ```skin``` - изображение скина в Base64
  ---
  #### Запрос:

  ```yaml
  {
    "method":"profile.getSkin",
    "key":"aaaaaaaaaaaaaaaaaaaa",
  }
  ```
  #### Вывод:
  ```yaml
  {
    "result":"0"
    "skin":"iVBORw0KGgoAAAANSUhEUgAAAEAAAABAAgMAAADXB5lNAAAADFBMVEUAAAARERFe+g////+TN9BUAAAAAXRSTlMAQObYZgAAAIlJREFUOMtjYGBYBQQMyIBGAqGh+ARWQQG9BZaGhkahunUVSBmYsYBMgVWhQG79X5BFq/+BRFbVAwX+/wcJTEcXqCcoUE6KFoi1SAKhoVi1xOMT+ItwKa2BaAjD0ANMqxBxj0gBpAkAo+V/1qoFoQ4wAWCg/7+FrASrwDd0gTKCKqghgOGOLIgAAEowACminP+4AAAAAElFTkSuQmCC"
  }
  ```
  ---
</details>

<details>
  <summary><code>setSkin</code></summary>
   
  ---
  # profile.setSkin
  
  Устанавливает скин для аккаунта. 
  
  Есть два способа поставить скин:
   - Использовать существующий скин - нужно указать путь к скину на сервере - ```category``` и ```name```.
   - Загрузить собственный скин, ииспользуя ```skin``` в формате Base64
  
  - **Запрос**
    * ```skin``` - Изображение скина 64x64 в Base64
    * ```category``` - Категория скина
    * ```name``` - Название скина

  - **Ответ**
    * ```result```
        - ```0``` - Скин успешно установлен
        - ```1``` - Не указаны нужные параметры
        - ```2``` - Неправильный формат скина
  ---
  #### Запрос:

  ```yaml
  {
    "method":"profile.getSkin",
    "category":"myCategory",
    "name":"skinName"
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
    
## Профиль (Общее)

<details>
  <summary><code>isLoginTaken</code></summary>
   
  ---
  # profiles.isLoginTaken
  
  Проверяет логин на доступность
  
  - **Запрос**
    * ```login``` - Логин для проверки

  - **Ответ**
    * ```result```
        - ```0``` - Логин не используется
        - ```1``` - Логин используется
  ---
  #### Запрос:

  ```yaml
  {
    "method":"profiles.isLoginTaken",
    "login":"myLogin",
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

## Скины

<details>
  <summary><code>getCategories</code></summary>
   
  ---
  # skins.getCategories
  
  Возвращает список категорий скинов
  
  - **Ответ**
    * ```categories``` - список категорий скинов
  ---
  #### Запрос:

  ```yaml
  {
    "method":"skins.getCategories",
  }
  ```
  #### Вывод:
  ```yaml
  {
    "result":"0",
    "categories":"cat1,cat2,cat3"
  }
  ```
  ---
</details>
    
<details>
  <summary><code>getCategoryPreview</code></summary>
   
  ---
  # skins.getCategoryPreview
  
  Возвращает скин из категории для предварительного просмотра
  
  - **Запрос**
    * ```category``` - Категория скинов

  - **Ответ**
    * ```skin``` - Изображение скина в Base64
  ---
  #### Запрос:

  ```yaml
  {
    "method":"skins.getCategoryPreview",
  }
  ```
  #### Вывод:
  ```yaml
  {
    "result":"0",
    "skin":"iVBORw0KGgoAAAANSUhEUgAAAEAAAABAAgMAAADXB5lNAAAADFBMVEUAAAARERFe+g////+TN9BUAAAAAXRSTlMAQObYZgAAAIlJREFUOMtjYGBYBQQMyIBGAqGh+ARWQQG9BZaGhkahunUVSBmYsYBMgVWhQG79X5BFq/+BRFbVAwX+/wcJTEcXqCcoUE6KFoi1SAKhoVi1xOMT+ItwKa2BaAjD0ANMqxBxj0gBpAkAo+V/1qoFoQ4wAWCg/7+FrASrwDd0gTKCKqghgOGOLIgAAEowACminP+4AAAAAElFTkSuQmCC"
  }
  ```
  ---
</details>
    
<details>
  <summary><code>getCategorySkins</code></summary>
   
  ---
  # skins.getCategorySkins
  
  - **Запрос**
    * ```category``` - Категория скинов

  - **Ответ**
    * ```skins``` - Список названий скинов в категории
  ---
  #### Запрос:

  ```yaml
  {
    "method":"skins.getCategorySkins",
  }
  ```
  #### Вывод:
  ```yaml
  {
    "result":"0",
    "skins":"skin1,skin2,skin3"
  }
  ```
  ---
</details>

<details>
  <summary><code>getSkin</code></summary>
   
  ---
  # skins.getSkin
  
  Возвращает скин по мени и категории
  
  - **Запрос**
    * ```category``` - Категория скинов
    * ```name``` - Название скина

  - **Ответ**
    * ```skin``` - Изображение скина в Base64
  ---
  #### Запрос:

  ```yaml
  {
    "method":"skins.getSkin",
  }
  ```
  #### Вывод:
  ```yaml
  {
    "result":"0",
    "skins":"iVBORw0KGgoAAAANSUhEUgAAAEAAAABAAgMAAADXB5lNAAAADFBMVEUAAAARERFe+g////+TN9BUAAAAAXRSTlMAQObYZgAAAIlJREFUOMtjYGBYBQQMyIBGAqGh+ARWQQG9BZaGhkahunUVSBmYsYBMgVWhQG79X5BFq/+BRFbVAwX+/wcJTEcXqCcoUE6KFoi1SAKhoVi1xOMT+ItwKa2BaAjD0ANMqxBxj0gBpAkAo+V/1qoFoQ4wAWCg/7+FrASrwDd0gTKCKqghgOGOLIgAAEowACminP+4AAAAAElFTkSuQmCC"
  }
  ```
  ---
</details>
    

