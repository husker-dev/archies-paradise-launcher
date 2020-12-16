# Документация по сетовому API (Устарела как старый айфон)
> Эта документация сделана специально для разработчика, который звбывает свои же методы. Он немного глупый... (Возможно ему просто лень всё помнить... непонятно...) 
Общение между клиентом и сервером происходит с помощью обычной отправки строк в две стороны.
Для хоть какой-то стандартизации используется JSON.
<details>
  <summary><code>Пример на Java</code></summary>
  
```java
// Подключаемся к серверу
Socket socket = new Socket("127.0.0.1", 15565);
// Отправляем запрос
new PrintWriter(socket.getOutputStream()).println("Тут должен быть JSON запрос");
// Получаем ответ
String received = new Scanner(socket.getInputStream()).nextLine();
// Отключаемся от сервера
socket.close();
```
</details>

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
  # auth.getAccessToken

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
  <summary><code>bindIp</code></summary>
  
  ---
  # user.bindIp
  Привязывает IP, с которого был сделан запрос, к аккаунту для дальнейшего входа на сервер
  
  ---
  #### Запрос:
  ```yaml
  {
    "method":"auth.bindIp",
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
    "key":"aaaaaaaaaaaaaaaaaaaa"
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
    "login":"myLogin"
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
  <summary><code>isIpBound</code></summary>
   
  ---
  # profiles.isIpBound
  
  Проверяет игрока на доступ к серверу
  
  - **Запрос**
    * ```name``` - Имя аккаунта
    * ```ip``` - IP, с которого был выполнен вход на сервер
  - **Ответ**
    * ```result```
        - ```0``` - Вход разрешён
        - ```1``` - Вход запрещён
  ---
  #### Запрос:
  ```yaml
  {
    "method":"profiles.isIpBound",
    "name":"myNickname",
    "ip":"127.0.0.1"
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
    "method":"skins.getCategories"
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
    "category":"cat1"
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
  
  Возвращает список названий скинов в категории
  
  - **Запрос**
    * ```category``` - Категория скинов
  - **Ответ**
    * ```skins``` - Список названий скинов в категории
  ---
  #### Запрос:
  ```yaml
  {
    "method":"skins.getCategorySkins",
    "category":"cat1"
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
    "method":"skins.getSkin"
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
    
## VK
<details>
  <summary><code>getPost</code></summary>
   
  ---
  # vk.getPost
  
  Возвращает запись в группе VK
  
  - **Запрос**
    * ```index``` - Номер записи
    * ```fields``` - Запрашиваемые параметры
      - ```type``` - Тип записи (```text```, ```picture```, ```video```, ```youtube```, ```snippet```)
      - ```text``` - Текст в записи
      - ```url``` - Ссылка на запись
      - ```image``` - Первое изображение в записи (Отсутствует у типа ```text```) 
  - **Ответ**
    * ```post``` - Информация о записи
  ---
  #### Запрос:
  ```yaml
  {
    "method":"vk.getPost",
    "index":"0",
    "fields":"type,text"
  }
  ```
  #### Вывод:
  ```yaml
  {
    "result":"0",
    "post":{
      "type":"text",
      "text":"Текст в записи VK... Да..."
    }
  }
  ```
  ---
</details>
<details>
  <summary><code>getInfo</code></summary>
   
  ---
  # vk.getInfo
  
  Возвращает информацию о группе VK
  
  - **Ответ**
    * ```title``` - Название группы
    * ```url``` - Ссылка на группу
    * ```description``` - Описание группы
    * ```image``` - Логотип группы в Base64
  ---
  #### Запрос:
  ```yaml
  {
    "method":"vk.getInfo"
  }
  ```
  #### Вывод:
  ```yaml
  {
    "result":"0",
    "title":"В гостях у Арчи :3",
    "url":"https://vk.com/archie.enotik",
    "description":"Подписавшись - станешь моим пушистиком :3",
    "image":"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"
  }
  ```
  ---
</details>

## YouTube
<details>
  <summary><code>getVideo</code></summary>
   
  ---
  # youtube.getVideo
  
  Возвращает видео с YouTube канала
  
  - **Запрос**
    * ```index``` - Номер видео
    * ```fields``` - Запрашиваемые параметры
      - ```title``` - Название видео
      - ```url``` - Ссылка на видео
      - ```date``` - Дата публикации видео
      - ```image``` - Превью видео
  - **Ответ**
    * ```video``` - Информация о видео
  ---
  #### Запрос:
  ```yaml
  {
    "method":"youtube.getVideo",
    "index":"0",
    "fields":"title,url"
  }
  ```
  #### Вывод:
  ```yaml
  {
    "result":"0",
    "post":{
      "title":"Название видео",
      "url":"https://www.youtube.com/watch?v=izGwDsrQ1eQ"
    }
  }
  ```
  ---
</details>
<details>
  <summary><code>getInfo</code></summary>
   
  ---
  # youtube.getInfo
  
  Возвращает информацию о канале YouTube
  
  - **Ответ**
    * ```title``` - Название канала
    * ```url``` - Ссылка на канал
    * ```subscribers``` - Количество подписчиков
    * ```logo``` - Логотип канала в Base64
  ---
  #### Запрос:
  ```yaml
  {
    "method":"youtube.getInfo"
  }
  ```
  #### Вывод:
  ```yaml
  {
    "result":"0",
    "title":"В гостях у Арчи :3",
    "url":"https://www.youtube.com/channel/UCAOEGtCBZUmctchRTkQ201A",
    "subscribers":"999999",
    "image":"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"
  }
  ```
  ---
</details>

## Клиент
<details>
  <summary><code>getInfo</code></summary>
   
  ---
  # client.getInfo
  
  Возвращает информацию об клиенте Minecraft
  
  - **Ответ**
    * ```build``` - Номер клиента (Основан на дате создания)
    * ```build_id``` - Идентификатор клиента (Основан на количестве предыдущих сборок)
    * ```version``` - Версия Minecraft
  ---
  #### Запрос:
  ```yaml
  {
    "method":"client.getInfo"
  }
  ```
  #### Вывод:
  ```yaml
  {
    "result":"0",
    "build":"20201001185731",
    "build_id":"3",
    "version":"1.16.1"
  }
  ```
  ---
</details>
<details>
  <summary><code>getModsInfo</code></summary>
   
  ---
  # client.getModsInfo
  
  Возвращает информацию об модификациях клиента. 
  
  Есть два способа получить список модов: 
  - Получить несколько первых элементов, указав параметр ```count```
  - Получить элемент по его индексу, указав ```index```
  
  - **Запрос**
    * ```count``` - Количество модификация
    * ```index``` - Индекс модификации
    * ```icon``` - Если равен ```true```, то возвращаются только модификации с иконкой 
  
  - **Ответ**
    * ```mods``` - Информация о модификациях
      - ```name``` - Название модификации
      - ```icon``` - Иконка модификации
      - ```description``` - Описание модификации
  ---
  #### Запрос:
  ```yaml
  {
    "method":"client.getModsInfo"
  }
  ```
  #### Вывод:
  ```yaml
  {
    "result":"0",
    "mods": [
        {
            "name": "Fabric API",
            "icon":"(Base64 icon)",
            "description": "Core API module providing key hooks and intercompatibility features."
        },
        {
            "name": "Sodium",
            "icon":"(Base64 icon)",
            "description": "Sodium is an free and open-source optimization mod for Minecraft which improves frame rates and reduces lag spikes."
        }
    ]
  }
  ```
  ---
</details>
<details>
  <summary><code>getFilesInfo</code></summary>
   
  ---
  # client.getFilesInfo
  
  Возвращает размер файлов и их архивированных копиях в байтах.  
  
  - **Ответ**
    * ```zip``` - Информация о zip файлах
      - ```versions``` - Размер архива versions.zip
      - ```mods``` - Размер архива mods.zip
      - ```other``` - Размер архива others.zip
    * ```folders``` - Информация о zip файлах
      - ```versions``` - Размер папки versions
      - ```mods``` - Размер папки mods
      - ```other``` - Размер всех файлов, не включая versions и mods 
  ---
  #### Запрос:
  ```yaml
  {
    "method":"client.getFilesInfo"
  }
  ```
  #### Вывод:
  ```yaml
  {
    "result":"0",
    "info": {
        "zip": {
            "versions": 18297788,
            "mods": 1007751,
            "other": 565079955
        },
        "folders": {
            "versions": 23817426,
            "mods": 1075929,
            "other": 765709286
        }
    }
  }
  ```
  ---
</details>
<details>
  <summary><code>checksum</code></summary>
   
  ---
  # client.checksum
  
  Сверяет MD5 хэш файлов клиента  
  
  - **Запрос**
    * ```mods``` - MD5 модификаций
    * ```client``` - MD5 .jar файла клиента
  
  - **Ответ**
    * ```equal_mods``` - Если ```true```, значит файлы модификаций совпадают
    * ```equal_client``` - Если ```true```, значит файл клиента совпадает
  ---
  #### Запрос:
  ```yaml
  {
    "method":"client.checksum",
    "mods":"8d45eaa12389b48eefd82f78e6290d3e",
    "client":"9b94beec05c9580343f663165fa53d3f"
  }
  ```
  #### Вывод:
  ```yaml
  {
    "result":"0",
    "equal_mods":"true",
    "equal_client":"false"
  }
  ```
  ---
</details>
<details>
  <summary><code>get</code></summary>
   
  ---
  # client.get
  
  Отправляет zip файл клиента
  
  <details>
  <summary><code>Пример скачивания файла на Java</code></summary>
  
  ```java
// Подключаемся к серверу
Socket socket = new Socket("127.0.0.1", 15565);
// Отправляем запрос
new PrintWriter(socket.getOutputStream()).println("{'method':'client.get','name':'mods'}".replaceAll("'", "\""));
// Создаём поток записи в файл
FileOutputStream fileOutputStream = new FileOutputStream("new_file.zip");
// Считываем размер файла (В данном примере не используется, но считать обязательно надо)
long size = Long.parseLong(new BufferedReader(new InputStreamReader(socket.getInputStream())).readLine());
// Считываем
byte[] dataBuffer = new byte[1024];
int len;
while ((len = socket.getInputStream().read(dataBuffer, 0, 1024)) != -1)
      fileOutputStream.write(dataBuffer, 0, len);
// Заканчиваем запись файла
fileOutputStream.close();
// Отключаемся
socket.close();
  ```
  </details>
  
  - **Запрос**
    * ```name``` - Название файла (```mods```, ```versions```, ```other```)
  
  - **Ответ**
    - Размер файла в байтах
    - Указанный Zip файл
  ---
  #### Запрос:
  ```yaml
  {
    "method":"client.get",
    "name":"mods"
  }
  ```
  #### Вывод:
  ```yaml
  18297788
  [file bytes]
  ```
  ---
</details>
