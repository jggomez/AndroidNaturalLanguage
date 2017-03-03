package co.devhack.naturallanguage;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.language.v1beta1.CloudNaturalLanguageAPI;
import com.google.api.services.language.v1beta1.CloudNaturalLanguageAPIScopes;
import com.google.api.services.language.v1beta1.model.AnalyzeEntitiesRequest;
import com.google.api.services.language.v1beta1.model.AnalyzeEntitiesResponse;
import com.google.api.services.language.v1beta1.model.Document;
import com.google.api.services.language.v1beta1.model.Entity;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import co.devhack.naturallanguage.models.EntityInfo;
import co.devhack.naturallanguage.token.AccessTokenLoader;

public class MainActivity extends AppCompatActivity {

    private String TAG = "MainActivity";

    // Constantes
    private final int LOAD_ACCESS_TOKEN = 1;
    private final String KEY_WIKIPEDIA_URL_METADATA = "wikipedia_url";

    // Views
    private EditText txtTextoAnalizar;
    private ProgressDialog progressDialog;

    //
    private GoogleCredential credential;

    // Declaración de la api de natural language
    private CloudNaturalLanguageAPI mApi = new CloudNaturalLanguageAPI.Builder(
            new NetHttpTransport(),
            JacksonFactory.getDefaultInstance(),
            new HttpRequestInitializer() {
                @Override
                public void initialize(HttpRequest request) throws IOException {
                    credential.initialize(request);
                }
            }
    ).build();

    // Evento del boton para analizar el texto
    private final View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            initAnalyze();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton btnAnalizar = (FloatingActionButton) findViewById(R.id.fabAnalizar);
        btnAnalizar.setOnClickListener(clickListener);

        initApi();

        txtTextoAnalizar = (EditText) findViewById(R.id.txtTextoAnalizar);

    }

    private void initAnalyze() {

        txtTextoAnalizar.clearFocus();
        analyzeEntities(txtTextoAnalizar.getText().toString());

    }

    private void analyzeEntities(final String textAnalyze) {

        new AsyncTask<Object, Void, AnalyzeEntitiesResponse>() {

            @Override
            protected void onPreExecute() {
                progressDialog = ProgressDialog.show(MainActivity.this, "Cargando", "Procesando texto");
            }

            @Override
            protected AnalyzeEntitiesResponse doInBackground(Object... objects) {
                try {
                    return mApi.documents().analyzeEntities(new AnalyzeEntitiesRequest()
                            .setDocument(
                                    new Document()
                                            .setContent(textAnalyze)
                                            .setType("PLAIN_TEXT"))).execute();
                } catch (IOException e) {
                    Log.e(TAG, "Error analizando las entidades " + e.getMessage());
                    return null;
                }
            }

            @Override
            protected void onPostExecute(AnalyzeEntitiesResponse responseAPI) {

                try {
                    final List<Entity> entities = responseAPI.getEntities();
                    final int size = entities.size();
                    final EntityInfo[] entityInfoArray = new EntityInfo[size];
                    EntityInfo entityInfo;
                    Map<String, String> metadata;

                    for (int i = 0; i < size; i++) {
                        entityInfo = new EntityInfo();
                        entityInfo.setName(entities.get(i).getName());
                        entityInfo.setType(entities.get(i).getType());
                        entityInfo.setSalience(entities.get(i).getSalience());
                        metadata = entities.get(i).getMetadata();

                        if (metadata != null && metadata.containsKey(KEY_WIKIPEDIA_URL_METADATA)) {
                            String wikiURL = metadata.get(KEY_WIKIPEDIA_URL_METADATA);
                            entityInfo.setWikipediaURL(wikiURL);
                        }

                        entityInfoArray[i] = entityInfo;

                        Log.i(TAG, "***Entidad Name" + entityInfo.getName());

                    }

                    showEntities(entityInfoArray);

                } catch (Exception e) {
                    Log.e(TAG, "Error onPostExecute: " + e.getMessage());
                }

                progressDialog.dismiss();
            }
        }.execute();

    }

    private void initApi() {

        getSupportLoaderManager().initLoader(LOAD_ACCESS_TOKEN, null,
                new LoaderManager.LoaderCallbacks<String>() {

                    @Override
                    public Loader<String> onCreateLoader(int id, Bundle args) {
                        return new AccessTokenLoader(MainActivity.this);
                    }

                    @Override
                    public void onLoadFinished(Loader<String> loader, String token) {
                        setAccessToken(token);
                    }

                    @Override
                    public void onLoaderReset(Loader<String> loader) {
                    }
                });
    }

    private void setAccessToken(String accessToken) {
        credential = new GoogleCredential()
                .setAccessToken(accessToken)
                .createScoped(CloudNaturalLanguageAPIScopes.all());
    }

    private void showEntities(EntityInfo[] entityInfoArray) {

        if (entityInfoArray != null && entityInfoArray.length > 0) {

            StringBuffer entitiesText = new StringBuffer();
            int lenEntityArray = entityInfoArray.length;
            EntityInfo entityInfoTmp;

            for (int i = 0; i < lenEntityArray; i++) {
                entityInfoTmp = entityInfoArray[i];
                entitiesText.append("Nombre => " + entityInfoTmp.getName());
                entitiesText.append("\nTipo => " + entityInfoTmp.getType());
                entitiesText.append("\nSalience => " + entityInfoTmp.getSalience());
                entitiesText.append("\nWIKI => " + entityInfoTmp.getWikipediaURL());
                entitiesText.append("\n*****************\n ");
            }

            new MaterialDialog.Builder(this)
                    .title("Entidades del texto")
                    .content(entitiesText.toString()).show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
