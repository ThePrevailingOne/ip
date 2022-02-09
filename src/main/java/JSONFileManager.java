import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

public class JSONFileManager {
    static private final String BASE_PATH = System.getProperty("user.dir");
    static private final Path SAVE_FOLDER_PATH = Paths.get(BASE_PATH, "src", "main", "data");
    static private final String FILE_NAME = "tasks.json";
    static private final Path SAVE_FILE_PATH = SAVE_FOLDER_PATH.resolve(FILE_NAME);

    private Path saveFolderPath;
    private Path saveFilePath;

    public JSONFileManager() {
        this.saveFolderPath = SAVE_FOLDER_PATH;
        this.saveFilePath = SAVE_FILE_PATH;
    }

    public JSONFileManager(String saveFolderPath, String jsonFileName) {
        String[] strPaths = saveFolderPath.split("/");
        this.saveFolderPath = Paths.get(BASE_PATH, strPaths);
        this.saveFilePath = this.saveFolderPath.resolve(jsonFileName);
    }
    private boolean isExistsSaveFolder() {
        return Files.exists(this.saveFolderPath);
    }

    private void createSaveFolder() {
        try {
            Files.createDirectories(this.saveFolderPath);
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private boolean isExistsSaveFile() {
        return Files.exists(saveFilePath);
    }

    private void createSaveFile() {
        try {
            Files.createFile(saveFilePath);
            saveListToJSONFile(new WordList());
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public void setUpSaveSystem() {
        if (!isExistsSaveFolder()) {
            createSaveFolder();
        }

        if (!isExistsSaveFile()) {
            createSaveFile();
        }
    }

    private String readSavedString() {
        try {
            String savedString = Files.readString(saveFilePath);
            return savedString;
        } catch (IOException e) {
            System.out.println(e);
            return "";
        }
    }

    private void writeSavedString(String savedString) {
        try {
            Files.writeString(saveFilePath, savedString);
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private String parseListToJSONString(WordList wordList) {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonWrapper = new JSONObject();

        wordList.forEach(wordListItem -> {
            JSONObject itemJSON = new JSONObject();
            String symbol = wordListItem instanceof Todo
                    ? Todo.getSymbol()
                    : wordListItem instanceof Deadline
                        ? Deadline.getSymbol()
                        : wordListItem instanceof Event
                            ? Event.getSymbol()
                            : null;
            itemJSON.put("symbol", symbol);
            itemJSON.put("description", wordListItem.getDescription());
            itemJSON.put("isDone", wordListItem.getIsDone());
//            System.out.println(itemJSON);
            if (wordListItem instanceof Deadline) {
                Deadline deadline = (Deadline) wordListItem;
                itemJSON.put("datetime", DateTimeManager.getOriginalString(deadline.getDatetime()));
            }
            if (wordListItem instanceof  Event) {
                Event deadline = (Event) wordListItem;
                itemJSON.put("datetime", DateTimeManager.getOriginalString(deadline.getDatetime()));
            }
            jsonArray.put(itemJSON);
        });

        return jsonArray.toString();
    }

    private WordList parseJSONStringToList(String jsonString) {
        JSONArray jsonArray = new JSONArray(jsonString);
        WordList wordList = new WordList();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String symbol = jsonObject.getString("symbol");
            if (symbol.equals(Todo.getSymbol())) {
                String description = jsonObject.getString("description");
                boolean isDone = jsonObject.getBoolean("isDone");
                wordList.storeTodo(description, isDone,false);
            } else if (symbol.equals(Deadline.getSymbol())) {
                String description = jsonObject.getString("description");
                boolean isDone = jsonObject.getBoolean("isDone");
                String datetimeString = jsonObject.getString("datetime");
                LocalDateTime datetime = DateTimeManager.parseString(datetimeString);
                wordList.storeDeadline(description, datetime, isDone,false);
            } else if (symbol.equals(Event.getSymbol())) {
                String description = jsonObject.getString("description");
                boolean isDone = jsonObject.getBoolean("isDone");
                String datetimeString = jsonObject.getString("datetime");
                LocalDateTime datetime = DateTimeManager.parseString(datetimeString);
                wordList.storeEvent(description, datetime, isDone,false);
            }
        }
        return wordList;
    }

    public WordList loadListFromJSONFile() {
        return parseJSONStringToList(readSavedString());
    }

    public void saveListToJSONFile(WordList wordList) {
        writeSavedString(parseListToJSONString(wordList));
    }
}