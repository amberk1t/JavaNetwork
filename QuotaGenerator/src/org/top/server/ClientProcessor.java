package org.top.server;


import org.top.communication.Receiver;
import org.top.communication.Sender;
import org.top.quotagen.IGenerator;

import java.io.IOException;
import java.net.Socket;

// класс-обработчик одного клиента
// запускается в отдельном потоке и работает с клиентом
public class ClientProcessor {

    // поля
    // TODO: добавить механизмы синхронизации при работе с isFree
    private boolean isFree;     // поле, которое указывает, свободен ли обработчик в данный момент
    public boolean isFree() {
        return isFree;
    }

    // сокета клиента, с которым работаем обработчик
    private Socket remoteClient;
    // генератор цитат
    private final IGenerator generator;

    // конструктор
    public ClientProcessor(IGenerator generator) {
        isFree = true;
        remoteClient = null;
        this.generator = generator;
    }

    // подготовка работы с клиентом
    public void prepareClient(Socket socket) throws Exception {
        if (!isFree) {
            throw new Exception("Not free clientProcessor!");
        }
        remoteClient = socket;
        isFree = false;
    }

    // работа с клиентом
    public void processClient() throws IOException {
        Sender sender = null;       // объект для отправки сообщений
        Receiver receiver = null;   // объект для получения сообщений
        String client = remoteClient.getInetAddress() + ":" + remoteClient.getPort();

        try {
            // объекты для отправки и получения данных
            sender = new Sender(remoteClient);
            receiver = new Receiver(remoteClient);

            // цикл работы с клиентом
            while (true) {
                // 1. читаем сообщение
                String msg = receiver.receiveMsg();
                System.out.println("server: receive msg from " + client);

                // 2. анализируем сообщение
                if (msg.equals("quota")) {
                    // то отправить цитату
                    System.out.println("server: get quota from " + client);
                    sender.sendMsg(generator.getRandomQuota());
                } else if (msg.equals("exit")) {
                    System.out.println("server: finish connected with" + client);
                    sender.sendMsg("bye");
                    break;
                } else {
                    sender.sendMsg("invalid command");
                    System.out.println("wrong msg from " + client);
                }
            }
        }
        catch (Exception ex) {
            System.out.println("Something wrong during processing client: " + ex.getMessage());
        }
        finally {
            // по окончанию цикла
            if (sender != null) {
                sender.close();
            }
            if (receiver != null) {
                receiver.close();
            }
            if (remoteClient != null && !remoteClient.isClosed()) {
                remoteClient.close();
            }

            // освободить исполнителя
            remoteClient = null;
            isFree = true;
        }
    }
}
