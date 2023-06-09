package itmo.app;

import itmo.app.client.Client;
import itmo.app.server.Server;

// TODO: Write help messages for every command
// TODO: cascade

/**
    1.  x Организовать хранение коллекции в реляционной СУБД (PostgresQL). Убрать хранение коллекции в файле. 
    2.  x Для генерации поля id использовать средства базы данных (sequence).
    3.  x Обновлять состояние коллекции в памяти только при успешном добавлении объекта в БД
    4.  x Все команды получения данных должны работать с коллекцией в памяти, а не в БД
    5.  x Организовать возможность регистрации и авторизации пользователей. У пользователя есть возможность указать пароль.
    6.  x Пароли при хранении хэшировать алгоритмом SHA-224
    7.  x Запретить выполнение команд не авторизованным пользователям.
    8.  x При хранении объектов сохранять информацию о пользователе, который создал этот объект.
    9.  x Пользователи должны иметь возможность просмотра всех объектов коллекции, но модифицировать могут только принадлежащие им.
    10. x Для идентификации пользователя отправлять логин и пароль с каждым запросом.

    
    Необходимо реализовать многопоточную обработку запросов.

    1. x Для многопоточного чтения запросов использовать создание нового потока (java.lang.Thread)
    2. x Для многопотчной обработки полученного запроса использовать Fixed thread pool
    3. x Для многопоточной отправки ответа использовать создание нового потока (java.lang.Thread)
    4. x Для синхронизации доступа к коллекции использовать java.util.Collections.synchronizedXXX
 */

public class App {

    public static void main(String[] args) {
        System.out.println("Set as the entry point one of the following:");
        System.out.println("Server: " + Server.class.getName());
        System.out.println("Client: " + Client.class.getName());
    }
}
