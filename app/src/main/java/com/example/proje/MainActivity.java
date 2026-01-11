package com.example.proje;

import android.app.AlertDialog; import android.content.ContentValues; import android.database.Cursor; import android.database.sqlite.SQLiteDatabase; import android.os.Bundle; import android.widget.*; import androidx.appcompat.app.AppCompatActivity; import java.util.ArrayList; import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    EditText editTitle, editRecipe;
    Spinner spinnerCategory, spinnerFilter;
    Button buttonSave;
    ListView listView;

    SQLiteDatabase db;

    ArrayList<String> recipeList = new ArrayList<>();
    ArrayList<Integer> idList = new ArrayList<>();
    ArrayAdapter<String> adapter;

    ArrayList<String> categories = new ArrayList<>(
            Arrays.asList("Kahvaltı", "Öğle Yemeği", "Akşam Yemeği", "Tatlı","İçecek","Diğer")
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTitle = findViewById(R.id.editTitle);
        editRecipe = findViewById(R.id.editRecipe);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerFilter = findViewById(R.id.spinnerFilter);
        buttonSave = findViewById(R.id.buttonSave);
        listView = findViewById(R.id.listView);

        ArrayAdapter<String> categoryAdapter =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_dropdown_item,
                        categories);

        spinnerCategory.setAdapter(categoryAdapter);

        ArrayList<String> filterList = new ArrayList<>();
        filterList.add("Tümü");
        filterList.addAll(categories);

        spinnerFilter.setAdapter(
                new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_dropdown_item,
                        filterList)
        );

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                recipeList
        );
        listView.setAdapter(adapter);

        db = openOrCreateDatabase(
                "RecipesDB",
                MODE_PRIVATE,
                null
        );

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS recipes (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "title TEXT," +
                        "category TEXT," +
                        "recipe TEXT)"
        );

        buttonSave.setOnClickListener(v -> saveRecipe());

        spinnerFilter.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent,
                                               android.view.View view,
                                               int position,
                                               long id) {
                        loadRecipes(
                                spinnerFilter.getSelectedItem().toString()
                        );
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                }
        );

        listView.setOnItemClickListener((parent, view, position, id) -> {

            int recipeId = idList.get(position);

            Cursor c = db.rawQuery(
                    "SELECT * FROM recipes WHERE id=?",
                    new String[]{String.valueOf(recipeId)}
            );

            if (c.moveToFirst()) {

                String title = c.getString(1);
                String category = c.getString(2);
                String recipe = c.getString(3);

                AlertDialog.Builder builder =
                        new AlertDialog.Builder(this);

                builder.setTitle("🍽️ " + title);
                builder.setMessage(
                        "Kategori: " + category + "\n\n" + recipe
                );

                builder.setPositiveButton("Düzenle", (d, w) -> {
                    editTitle.setText(title);
                    editRecipe.setText(recipe);

                    spinnerCategory.setSelection(
                            categories.indexOf(category)
                    );

                    buttonSave.setText("Güncelle");

                    buttonSave.setOnClickListener(v -> {
                        ContentValues values = new ContentValues();
                        values.put("title",
                                editTitle.getText().toString());
                        values.put("category",
                                spinnerCategory.getSelectedItem().toString());
                        values.put("recipe",
                                editRecipe.getText().toString());

                        db.update("recipes",
                                values,
                                "id=?",
                                new String[]{String.valueOf(recipeId)});

                        resetForm();
                        loadRecipes(
                                spinnerFilter.getSelectedItem().toString()
                        );
                    });
                });

                builder.setNegativeButton("Sil", (d, w) -> {
                    db.delete("recipes",
                            "id=?",
                            new String[]{String.valueOf(recipeId)});
                    loadRecipes(
                            spinnerFilter.getSelectedItem().toString()
                    );
                });

                builder.setNeutralButton("İptal", null);
                builder.show();
            }
            c.close();
        });

        loadRecipes("Tümü");
    }

    private void saveRecipe() {
        if (editTitle.getText().toString().trim().isEmpty()
                || editRecipe.getText().toString().trim().isEmpty()) {
            Toast.makeText(this,
                    "Alanlar boş bırakılamaz",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put("title", editTitle.getText().toString());
        values.put("category",
                spinnerCategory.getSelectedItem().toString());
        values.put("recipe", editRecipe.getText().toString());

        db.insert("recipes", null, values);
        resetForm();
        loadRecipes(spinnerFilter.getSelectedItem().toString());
    }

    private void resetForm() {
        editTitle.setText("");
        editRecipe.setText("");
        buttonSave.setText("Kaydet");
        buttonSave.setOnClickListener(v -> saveRecipe());
    }

    private void loadRecipes(String filter) {
        recipeList.clear();
        idList.clear();

        Cursor c;
        if (filter.equals("Tümü")) {
            c = db.rawQuery(
                    "SELECT id,title,category FROM recipes", null);
        } else {
            c = db.rawQuery(
                    "SELECT id,title,category FROM recipes WHERE category=?",
                    new String[]{filter});
        }

        while (c.moveToNext()) {
            idList.add(c.getInt(0));
            recipeList.add(
                    "🍴 " + c.getString(1) +
                            "\n" + c.getString(2)
            );
        }
        c.close();
        adapter.notifyDataSetChanged();
    }
}
