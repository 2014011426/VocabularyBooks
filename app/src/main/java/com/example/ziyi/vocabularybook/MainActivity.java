package com.example.ziyi.vocabularybook;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONObject;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {


    WordsDBHelper wordsDBHelper;

    private String YouDaoBaseUrl = "http://fanyi.youdao.com/openapi.do";
    private String YouDaoKeyFrom = "haobaoshui";
    private String YouDaoKey = "1650542691";
    private String YouDaoType = "data";
    private String YouDaoDoctype = "json";
    private String YouDaoVersion = "1.1";
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listView = (ListView) findViewById(R.id.listview);
        registerForContextMenu(listView);

        wordsDBHelper = new WordsDBHelper(this);

        ArrayList<Map<String, String>> items = getAll();
        setWordsListView(items);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    protected void onDestroy() {
        super.onDestroy();
        wordsDBHelper.close();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();

        switch (id) {
            case R.id.action_search:
                SearchDialog();
                return true;
            case R.id.action_add:
                InsertDialog();
                return true;
            case R.id.online_search:
                OnlineSearchDialog();
                return true;

        }
        return super.onOptionsItemSelected(menuItem);
    }

    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo infor) {
        super.onCreateOptionsMenu(contextMenu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_concent, contextMenu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        TextView textID = null;
        TextView textWord = null;
        TextView textMeaning = null;
        TextView textSample = null;
        AdapterView.AdapterContextMenuInfo info = null;
        View itemView = null;

        switch (item.getItemId()) {
            case R.id.menu_update:
                info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                itemView = info.targetView;
                textID = (TextView) itemView.findViewById(R.id.textID);
                textWord = (TextView) itemView.findViewById(R.id.textViewWord);
                textMeaning = (TextView) itemView.findViewById(R.id.textViewMeaning);
                textSample = (TextView) itemView.findViewById(R.id.textViewSample);
                if (textID != null && textWord != null && textMeaning != null && textSample != null) {
                    String strID = textID.getText().toString();
                    String strWord = textWord.getText().toString();
                    String strMeaning = textMeaning.getText().toString();
                    String strSample = textSample.getText().toString();
                    UpdateDialog(strID, strWord, strMeaning, strSample);
                }
                break;
            case R.id.menu_delete:
                info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                itemView = info.targetView;
                textID = (TextView) itemView.findViewById(R.id.textID);
                if (textID != null) {
                    String strID = textID.getText().toString();
                    DeleteDialog(strID);
                }
                break;
        }
        return super.onContextItemSelected(item);
    }

    private void setWordsListView(ArrayList<Map<String, String>> items) {
        SimpleAdapter simpleAdapter = new SimpleAdapter(this, items, R.layout.item_layout,
                new String[]{Words.Word._ID, Words.Word.COLUMN_NAME_WORD, Words.Word.COLUMN_NAME_MEANING, Words.Word.COLUMN_NAME_SAMPLE},
                new int[]{R.id.textID, R.id.textViewWord, R.id.textViewMeaning, R.id.textViewSample});

        ListView listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(simpleAdapter);
    }

    private ArrayList<Map<String, String>> getAll() {
        SQLiteDatabase db = wordsDBHelper.getWritableDatabase();

        String[] projection = {
                Words.Word._ID,
                Words.Word.COLUMN_NAME_WORD,
                Words.Word.COLUMN_NAME_MEANING,
                Words.Word.COLUMN_NAME_SAMPLE
        };

        String sortOrder = Words.Word.COLUMN_NAME_WORD + " DESC";

        Cursor cursor = db.query(
                Words.Word.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );

        return ConvertCursor2List(cursor);
    }

    private ArrayList<Map<String, String>> ConvertCursor2List(Cursor cursor) {
        ArrayList<Map<String, String>> result = new ArrayList<>();
        while (cursor.moveToNext()) {
            Map<String, String> map = new HashMap<>();
            map.put(Words.Word._ID, String.valueOf(cursor.getInt(0)));
            map.put(Words.Word.COLUMN_NAME_WORD, cursor.getString(1));
            map.put(Words.Word.COLUMN_NAME_MEANING, cursor.getString(2));
            map.put(Words.Word.COLUMN_NAME_SAMPLE, cursor.getString(3));
            result.add(map);
        }
        return result;
    }

    private void Insert(String strWord, String strMeaning, String strSample) {
        SQLiteDatabase db = wordsDBHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(Words.Word.COLUMN_NAME_WORD, strWord);
        contentValues.put(Words.Word.COLUMN_NAME_MEANING, strMeaning);
        contentValues.put(Words.Word.COLUMN_NAME_SAMPLE, strSample);

        long newRowID;
        newRowID = db.insert(
                Words.Word.TABLE_NAME,
                null,
                contentValues
        );
    }


    private void InsertDialog() {
        final TableLayout tableLayout = (TableLayout) getLayoutInflater().inflate(R.layout.insert, null);
        new AlertDialog.Builder(this)
                .setTitle("新增单词")
                .setView(tableLayout)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String strWord = ((EditText) tableLayout.findViewById(R.id.txtWord)).getText().toString();
                                String strMeaning = ((EditText) tableLayout.findViewById(R.id.txtMeaning)).getText().toString();
                                String strSample = ((EditText) tableLayout.findViewById(R.id.txtSample)).getText().toString();

                                Insert(strWord, strMeaning, strSample);

                                setWordsListView(getAll());
                            }
                        }
                )
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create()
                .show();
    }

    private void DeleteUseSQL(String strID) {
        String sql = "delete from words where _id = ' " + strID + " ' ";

        SQLiteDatabase db = wordsDBHelper.getReadableDatabase();
        db.execSQL(sql);
    }

    private void DeleteDialog(final String strID) {
        new AlertDialog.Builder(this).setTitle("删除单词")
                .setMessage("确定删除单词？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DeleteUseSQL(strID);

                        setWordsListView(getAll());
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create()
                .show();
    }

    private void UpdateUseSQL(String strID, String strWord, String strMeaning, String strSample) {
        SQLiteDatabase db = wordsDBHelper.getReadableDatabase();
        String sql = "update words set word = ?,meaning = ?,sample = ? where _id = ?";
        db.execSQL(sql, new String[]{strWord, strMeaning, strSample, strID});
    }

    private void UpdateDialog(final String strID, final String strWord, final String strMeaning, final String strSample) {
        final TableLayout tableLayout = (TableLayout) getLayoutInflater().inflate(R.layout.insert, null);
        ((EditText) tableLayout.findViewById(R.id.txtWord)).setText(strWord);
        ((EditText) tableLayout.findViewById(R.id.txtMeaning)).setText(strMeaning);
        ((EditText) tableLayout.findViewById(R.id.txtSample)).setText(strSample);

        new AlertDialog.Builder(this)
                .setTitle("修改单词")
                .setView(tableLayout)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String newWord = ((EditText) tableLayout.findViewById(R.id.txtWord)).getText().toString();
                        String newMeaning = ((EditText) tableLayout.findViewById(R.id.txtMeaning)).getText().toString();
                        String newSample = ((EditText) tableLayout.findViewById(R.id.txtSample)).getText().toString();

                        UpdateUseSQL(strID, newWord, newMeaning, newSample);

                        setWordsListView(getAll());
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create()
                .show();
    }

    private ArrayList<Map<String, String>> SearchUseSQL(String searchWord) {
        SQLiteDatabase db = wordsDBHelper.getReadableDatabase();
        String sql = "select * from words where word like ? order by word desc";
        Cursor cursor = db.rawQuery(sql, new String[]{"%" + searchWord + "%"});
        return ConvertCursor2List(cursor);
    }

    private void SearchDialog() {
        final TableLayout tableLayout = (TableLayout) getLayoutInflater().inflate(R.layout.search, null);

        new AlertDialog.Builder(this)
                .setTitle("查找单词")
                .setView(tableLayout)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String searchWord = ((EditText) tableLayout.findViewById(R.id.search_word)).getText().toString();

                        ArrayList<Map<String, String>> items = SearchUseSQL(searchWord);

                        if (items.size() > 0) {
                            setWordsListView(items);
                        } else Toast.makeText(MainActivity.this, "没有找到", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create()
                .show();
    }

    private void OnlineSearchDialog() {
        final TableLayout tableLayout = (TableLayout) getLayoutInflater().inflate(R.layout.online_search, null);

        new AlertDialog.Builder(this)
                .setTitle("查找单词")
                .setView(tableLayout)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String searchWord = ((EditText) tableLayout.findViewById(R.id.search_word_online)).getText().toString();

                        ArrayList<Map<String, String>> items = null;
                        try {
                            items = SearchUseInternet(searchWord);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (items.size() > 0) {
                            setWordsListView(items);
                        } else Toast.makeText(MainActivity.this, "没有找到", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create()
                .show();

    }

    private ArrayList<Map<String, String>> SearchUseInternet(String searchWord) throws Exception {
        ArrayList<Map<String, String>> result = new ArrayList<>();

        String YouDaoUrl = YouDaoBaseUrl + "?keyfrom=" + YouDaoKeyFrom
                + "&key=" + YouDaoKey + "&type=" + YouDaoType + "&doctype="
                + YouDaoDoctype + "&type=" + YouDaoType + "&version="
                + YouDaoVersion + "&q=" + searchWord;

        String s1 = AnalyzingOfJson(YouDaoUrl);
        String s2 = "000";

        Map<String, String> map = new HashMap<>();
        map.put(s2,s1);
        result.add(map);

        return result;
    }

    private String AnalyzingOfJson(String youDaoBaseUrl) throws Exception {
        // 第一步，创建HttpGet对象
        //HttpGet httpGet = new HttpGet(url);
        // 第二步，使用execute方法发送HTTP GET请求，并返回HttpResponse对象
       // HttpResponse httpResponse = new DefaultHttpClient().execute(httpGet);

        String results = null;
        URL url = null;
        HttpURLConnection connection = null;
        InputStreamReader in = null;
        int code = 0;
        try {
            url = new URL(youDaoBaseUrl);
            connection = (HttpURLConnection) url.openConnection();
            in = new InputStreamReader(connection.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(in);
            StringBuffer strBuffer = new StringBuffer();
            String line = null;
            code = connection.getResponseCode();

            while ((line = bufferedReader.readLine()) != null) {
                strBuffer.append(line);
            }
            results = strBuffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        if (code == 200) {
            // 第三步，使用getEntity方法活得返回结果
            String result = results;
            System.out.println("result:" + result);
            JSONArray jsonArray = new JSONArray("[" + result + "]");
            String message = null;
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject != null) {
                    String errorCode = jsonObject.getString("errorCode");
                    if (errorCode.equals("20")) {
                        Toast.makeText(getApplicationContext(), "要翻译的文本过长",
                                Toast.LENGTH_SHORT);
                    } else if (errorCode.equals("30 ")) {
                        Toast.makeText(getApplicationContext(), "无法进行有效的翻译",
                                Toast.LENGTH_SHORT);
                    } else if (errorCode.equals("40")) {
                        Toast.makeText(getApplicationContext(), "不支持的语言类型",
                                Toast.LENGTH_SHORT);
                    } else if (errorCode.equals("50")) {
                        Toast.makeText(getApplicationContext(), "无效的key",
                                Toast.LENGTH_SHORT);
                    } else {
                        // 要翻译的内容
                        String query = jsonObject.getString("query");
                        message = query;
                        // 翻译内容
                        String translation = jsonObject
                                .getString("translation");
                        message += "\t" + translation;
                        // 有道词典-基本词典
                        if (jsonObject.has("basic")) {
                            JSONObject basic = jsonObject
                                    .getJSONObject("basic");
                            if (basic.has("phonetic")) {
                                String phonetic = basic.getString("phonetic");
                                message += "\n\t" + phonetic;
                            }
                            if (basic.has("explains")) {
                                String explains = basic.getString("explains");
                                message += "\n\t" + explains;
                            }
                        }
                        // 有道词典-网络释义
                        if (jsonObject.has("web")) {
                            String web = jsonObject.getString("web");
                            JSONArray webString = new JSONArray("[" + web + "]");
                            message += "\n网络释义：";
                            JSONArray webArray = webString.getJSONArray(0);
                            int count = 0;
                            while (!webArray.isNull(count)) {

                                if (webArray.getJSONObject(count).has("key")) {
                                    String key = webArray.getJSONObject(count)
                                            .getString("key");
                                    message += "\n\t<" + (count + 1) + ">"
                                            + key;
                                }
                                if (webArray.getJSONObject(count).has("value")) {
                                    String value = webArray
                                            .getJSONObject(count).getString(
                                                    "value");
                                    message += "\n\t   " + value;
                                }
                                count++;
                            }
                        }
                    }
                }
            }
            //text.setText(message);
            Log.v("tag",message);
            return message;
        } else {
            Toast.makeText(getApplicationContext(), "提取异常", Toast.LENGTH_SHORT);
            return "error";
        }
    }





    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }



    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}
