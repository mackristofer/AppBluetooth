package br.m3tec.appbluetooth;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class ListarDispositivos extends ListActivity {

    private BluetoothAdapter adapter = null; //adaptador bluetooth para pegar o device
    static String ENDERECO_MAC = null;//endereco mac de retorno

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ArrayAdapter<String> list = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);//adaptador para criar a lista

        adapter = BluetoothAdapter.getDefaultAdapter();//pega o adaptador atual

        Set<BluetoothDevice> devices = adapter.getBondedDevices();//gera uma lista para armazenar os devices pareados

        if(devices.size() > 0){//checa se existe algum pareado
            for(BluetoothDevice device : devices){//pega um por um e exibe na lista
                String nomeBt = device.getName();//pega o nome
                String macBt = device.getAddress();//pega o mac
                list.add(nomeBt + "\n"+macBt);//add na lista
            }
        }
        setListAdapter(list);//crio e exibo a lista

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        String infos = ((TextView) v).getText().toString();//pega as informacoes do text view que tem mac e nome do devce
        //Toast.makeText(getApplicationContext(), infos ,Toast.LENGTH_LONG).show();

        String btMac = infos.substring(infos.length() - 17);//o mac tem 17 caracter, entao peg a informacao menos 17 e terei apenas o endreco mac
        //Toast.makeText(getApplicationContext(), btMac ,Toast.LENGTH_LONG).show();

        Intent retornaMac = new Intent();//intente de retorno para a main principal
        retornaMac.putExtra(ENDERECO_MAC, btMac);//armazeno o mac do device na variavel ENDERECO_MAC que vai para a main principal
        setResult(RESULT_OK, retornaMac);//seto o resultado como ok para tratar na no activityresult
        finish();//finalizo a lista

    }
}
