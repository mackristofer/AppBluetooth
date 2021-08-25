package br.m3tec.appbluetooth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {


    Button btnAtiva, btnConectar, btnLed, btnLed2, btnLed3, btnBuscar; //instancia os botoes

    private static final int ATIVA_BT = 1;//retorno para activity result de ativacao
    private static final int LISTA_BT = 2;//retorno para activity result de listardispostvs
    private static final int MESSAGE_READ = 3;//retorno handler threade de inputstream do bluetooth.
    private static final int BUSCA_BT = 4;//retoro para busca de novos bluetooth

    BluetoothAdapter adapter = null;//representacao adaptador bluetooth
    BluetoothDevice btDevice = null;//device remoto que sera conectado
    BluetoothSocket btSocket = null;//socket para conexao e tratamento da conexao do device
    ConnectedThread connect;//thread para interacao com o bluetooth
    Handler mHandler;//handler de retorno para atualizar a interface principal

    StringBuilder dataBt;//armazena os dados de input do bluetooth em tempo real

    UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");//uuid usada para abertura de canal rfcom

    boolean conexao = false;//para testar o status da conexao.
    private static String MAC = null;//string que contem o mac capiturado do device.

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btnAtiva = findViewById(R.id.btnativar);//associa ao botao
        btnConectar = findViewById(R.id.btnconectarr);//associa ao botao
        btnLed = findViewById(R.id.btnled);//associa ao botao
        btnLed2 = findViewById(R.id.btnled2);
        btnLed3 = findViewById(R.id.btnled3);
        btnBuscar = findViewById(R.id.btnbuscar);
        adapter = BluetoothAdapter.getDefaultAdapter();//capitura o adaptador bluetooth atual
        /*
         Testa o aparelho contem um hadware bluetooth.
         Se igual a null, nao existe o hardware, caso contrario vamos checar se ele estar ativo.
         se nao estiver ativo, abre uma intent padrao do sistema solicitando ativacao para o usuario
         */
        if (adapter == null) {
            Toast.makeText(getApplicationContext(), "Seu dispositivo não possui bluetooth", Toast.LENGTH_LONG).show();
        } else if (!adapter.isEnabled()) {
            Intent ativaBt = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);//chama intent padrao do android ara tivar o bluetooth
            startActivityForResult(ativaBt, ATIVA_BT);//retorno da intent.

        }
        /*
        fim de teste e ativacao do bluetooth.
         */

        /*
        Logica maluca apara o botao de desativar o bluetooth.
         */
        btnAtiva.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(adapter.isEnabled()){
                    adapter.disable();
                    finish();
                }else{
                    Intent ativaBt = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);//chama intent padrao do android ara tivar o bluetooth
                    startActivityForResult(ativaBt, ATIVA_BT);//retorno da intent.
                                    }
            }
        });

        /*
        botao conectar sera o responsavel por iniciar a conexao com o device pareado com o sistema.
        vamos alerar o texto do botao de acordo com o status da conexao, alem é clado de fechar o nosso
        socket.
        caso não esteja conectado ele vai chamar a tela que lista os dispostivos pareados com o sistema
        para a conexao.
         */
        btnConectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (conexao) {
                    //desconectar
                    try {
                        btSocket.close();//fecha o socket, finaliza a conexao
                        conexao = false;//informa que a conexao foi fechada
                        btnConectar.setText("Conectar");//altera o texto do botao
                        Toast.makeText(getApplicationContext(), "Desconectado.", Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        conexao = false;//garante o status da conexao mesmo se der algum erro de IO
                        Toast.makeText(getApplicationContext(), "Erro: " + e, Toast.LENGTH_LONG).show();
                    }

                } else {
                    /*
                    Responsave por chamar a lista de dispositivos pareados, essa lista sempre vai retornar o
                    mac do device selecionado.
                     */
                    Intent listaBt = new Intent(MainActivity.this, ListarDispositivos.class);//intent para abrir tela com lista de dispositivos pareados.
                    startActivityForResult(listaBt, LISTA_BT);//resposta com o mac adress para inicio da conexao
                }
            }
        });

        /*
        botao responsavel por ligar e desligar o led la no arduino
        quando clico no botao ele envia uma string para o arduino, o codigo esta prepaarado para atuar assim que
        receber a informacao.
         */
        btnLed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (conexao) {
                    connect.enviar("led1");//envia a informacao, usando a connectionthread
                    Toast.makeText(getApplicationContext(), "Enviado", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Bluetooth não esta conectado.", Toast.LENGTH_LONG).show();
                }
            }
        });

                /*
        botao responsavel por ligar e desligar o led la no arduino
        quando clico no botao ele envia uma string para o arduino, o codigo esta prepaarado para atuar assim que
        receber a informacao.
         */
        btnLed2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (conexao) {
                    connect.enviar("led2");//envia a informacao, usando a connectionthread
                    Toast.makeText(getApplicationContext(), "Enviado", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Bluetooth não esta conectado.", Toast.LENGTH_LONG).show();
                }
            }
        });

                /*
        botao responsavel por ligar e desligar o led la no arduino
        quando clico no botao ele envia uma string para o arduino, o codigo esta prepaarado para atuar assim que
        receber a informacao.
         */
        btnLed3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (conexao) {
                    connect.enviar("led3");//envia a informacao, usando a connectionthread
                    Toast.makeText(getApplicationContext(), "Enviado", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Bluetooth não esta conectado.", Toast.LENGTH_LONG).show();
                }
            }
        });

        /*

        Busca novos dispositivos.


         */

        btnBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent buscaBt = new Intent(MainActivity.this, BuscaDispositivos.class);//intent para abrir tela com lista de dispositivos pareados.
                startActivityForResult(buscaBt, BUSCA_BT);//resposta com o mac adress para inicio da conexao
            }
        });



        /*
        handler necessário para atualizar a interface grafica, o android n permite que outras telas
        atualizem a interface principal.
        esse handle capitura toda vez que receber uma mensagem no inpustream, essa mensagem sempre sera validada
        por um protocolo para ser usada.
         */

        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {//metodo abstrato handle para atualizacao da interface
                if(msg.what == MESSAGE_READ){//se houve uma msg do tipo MESSAGE_READ, nos interessa e temos que tratar
                    dataBt = new StringBuilder();//inicializo a stringbuilder
                    String receive = (String) msg.obj;//recebo uma sring conforme recebido e convertido os dados no inputstram
                    dataBt.append(receive);//adiciona os dados na stringbuild em tempo real

                    int fimInformacao = dataBt.indexOf("}");//pega  local delimitado cmo fim da nossa string que vem do arduino, delimitado pela }

                    if(fimInformacao > 0){//se for maior q zero, ou seja recebeu algo mais o caracter }
                        String data = dataBt.substring(0, fimInformacao);//string que recebe do inicio ao fim da informacao
                        int tamInformacao = data.length();//recebe o tamanho da string com a informacao

                        if(dataBt.charAt(0) == '{'){//retorna true se o char que esta no indice 0 contem o caracter { que informa o inicio da informacao
                            /*
                            Se chegamos ate aqui quer dizer que a informação esta completa, ou seja ela existe inicio { e fim } conforme
                            convencionado no microcontrolador arduino.
                             */

                            String datareceive = dataBt.substring(1, tamInformacao);//recebe a informacao sem as {}
                            Log.d("Recebido", datareceive);

                            if(datareceive.contains("led1on")){
                                btnLed.setText("LIGADO");
                            }else if(datareceive.contains("led1of")){
                                btnLed.setText("DESLIGADO");
                            }

                            if(datareceive.contains("led2on")){
                                btnLed2.setText("LIGADO");
                            }else if(datareceive.contains("led2of")){
                                btnLed2.setText("DESLIGADO");
                            }

                            if(datareceive.contains("led3on")){
                                btnLed3.setText("LIGADO");
                            }else if(datareceive.contains("led3of")){
                                btnLed3.setText("DESLIGADO");
                            }

                        }

                        dataBt.delete(0, dataBt.length());//metodo responsavel por limpar a string build apos seu uso.
                    }
                }
            }
        };

    }



    /*
    Activity result é a responsavel por tratar as respostas que vem das outras telas, para android isso
    sempre sera necessario uma vez que ele n permite que outras telas atualize a tela principal.
     */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {//criei um swtich que pega o requestcode da intent
            case ATIVA_BT://retorno da intent padrao do sistema que solicita ativacao do bluetooth
                if (resultCode == Activity.RESULT_OK) {//resposta ok do sistema.
                    Toast.makeText(getApplicationContext(), "Bluetooth foi ativado", Toast.LENGTH_LONG).show();
                    btnAtiva.setText("Desativar");
                } else {
                    Toast.makeText(getApplicationContext(), "Não foi possível ativar, app será encerrado.", Toast.LENGTH_LONG).show();
                    finish();//se o usuario não aceitar ativar simplismente fecha a aplicacao.
                }
                break;

            case LISTA_BT://se abrimos a lista de disositivos pareados
                if (resultCode == Activity.RESULT_OK) {//resultado ok, salvo o MAC adrress do endereco obtido na lista
                    MAC = data.getExtras().getString(ListarDispositivos.ENDERECO_MAC);//armazeno o endereco na variavel MAC, vou usar para me conectar ao device
                    //Toast.makeText(getApplicationContext(), "MAC"+MAC,Toast.LENGTH_LONG).show();
                    btDevice = adapter.getRemoteDevice(MAC);//crio uma representacao do device remoto com o endereco mac especificado

                    try {
                        btSocket = btDevice.createRfcommSocketToServiceRecord(myUUID);//solicito abertura do canal frcomm, passando a uuid apropriada
                        btSocket.connect();//crio o socket para o device
                        conexao = true;//habilito conexao true
                        connect = new ConnectedThread(btSocket);//crio uma nova thread passando o socket como parametro para seu construtor
                        connect.start();//inicio a thread para escutar a conexao
                        btnConectar.setText("Desconectar");//altero o texto do botao
                        Toast.makeText(getApplicationContext(), "Conectado", Toast.LENGTH_LONG).show();

                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(), "Erro: " + e, Toast.LENGTH_LONG).show();
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "Falha obter o mac", Toast.LENGTH_LONG).show();

                }
                break;

            case BUSCA_BT://trata o retorno da busca de novos dispositivos, faz exatamente o que o lista pareados faz.
        }
    }

    /*
    connectedTread modelo do proprio site developer, a diferenca que essa vai trabalhar diretamente com a string,
    fazendo a conversao para bytes apenas na hora de enviar e receber do arduino.
    a escuta por novas mensagem seram executadas em thread separada afim de n travar o sistema.
     */

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;//objeto inputstream para pegar o que vem do arduino
        private final OutputStream mmOutStream;//objeto outputstream para mandar algo para o arduino

        public ConnectedThread(BluetoothSocket socket) {//construtor receber um bluetoothsocket como parametro
            InputStream tmpIn = null;//objeto inputstream temporario
            OutputStream tmpOut = null;//objeto inputstream temporario

            try {
                tmpIn = socket.getInputStream();//obtem os dados da porta input
                tmpOut = socket.getOutputStream();//escreve dados na porta output
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {

                    bytes = mmInStream.read(buffer);//leio o buffer em bytes na output do device
                    String datain = new String(buffer, 0, bytes);//converto o byte para string o que vou enviar para meu handler e tratar posteriomente
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, datain).sendToTarget();//envio para o handle
                } catch (IOException e) {
                    break;
                }
            }
        }


        /* methodo que recebe uma string para enviar para o dispositivo conectado */
        public void enviar(String dataout) {
            byte[] bytes = dataout.getBytes();//converte a string em bytes
            try {
                mmOutStream.write(bytes);//envia os bytes para o disposistivo.
            } catch (IOException e) {
            }
        }

    }



}
