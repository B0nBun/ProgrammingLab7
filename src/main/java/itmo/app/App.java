package itmo.app;

import itmo.app.client.Client;
import itmo.app.server.Server;

/**
 * TODO commands:
 *
 * add if max
 * count greater than fuel type
 * exit
 * filter greater than
 * group counting by id
 * remove by id
 * remove lower
 * update
 *
 * TODO: Write help messages for every command
 */

/**
    1. Организовать хранение коллекции в реляционной СУБД (PostgresQL). Убрать хранение коллекции в файле. x
    2. Для генерации поля id использовать средства базы данных (sequence). x
    3. Обновлять состояние коллекции в памяти только при успешном добавлении объекта в БД
    4. Все команды получения данных должны работать с коллекцией в памяти, а не в БД
    5. Организовать возможность регистрации и авторизации пользователей. У пользователя есть возможность указать пароль. x
    6. Пароли при хранении хэшировать алгоритмом SHA-224 x
    7. Запретить выполнение команд не авторизованным пользователям. x
    8. При хранении объектов сохранять информацию о пользователе, который создал этот объект. x
    9. Пользователи должны иметь возможность просмотра всех объектов коллекции, но модифицировать могут только принадлежащие им.
    10. Для идентификации пользователя отправлять логин и пароль с каждым запросом. x

    
    Необходимо реализовать многопоточную обработку запросов.

    1. Для многопоточного чтения запросов использовать создание нового потока (java.lang.Thread) x
    2. Для многопотчной обработки полученного запроса использовать Fixed thread pool x
    3. Для многопоточной отправки ответа использовать создание нового потока (java.lang.Thread) x
    4. Для синхронизации доступа к коллекции использовать java.util.Collections.synchronizedXXX
 */

public class App {

    public static void main(String[] args) {
        System.out.println("Set as the entry point one of the following:");
        System.out.println("Server: " + Server.class.getName());
        System.out.println("Client: " + Client.class.getName());
    }
}
