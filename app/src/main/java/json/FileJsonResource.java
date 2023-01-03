package json;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.json.JSONObject;

public class FileJsonResource {
  private final String pathFolder;
  private final List<File> pathFiles;
  private final HashMap<String, JSONObject> jsonObjects;

  FileJsonResource(String pathFolder, boolean isFileResource) {
    this.pathFolder = pathFolder;
    this.pathFiles = isFileResource ? this.getListFileResouce() : this.getListFile();
    this.jsonObjects = this.readJsonFromDataFiles();
  }

  private InputStream readFromPathFile(File file) throws IOException {
    return new FileInputStream(file);
  }

  private OutputStream writeFromPathfile(File file, Boolean append) throws IOException {
    return new FileOutputStream(file, append);
  }

  private InputStreamReader readFromInputStream(InputStream inputStream) throws IOException {
    return new InputStreamReader(inputStream);
  }

  private OutputStreamWriter writerFromOuputStream(OutputStream outputStream) throws IOException {
    return new OutputStreamWriter(outputStream);
  }

  private String readFromInputStreamReader(InputStreamReader inputStreamReader) throws IOException {
    StringBuilder resultStringBuilder = new StringBuilder();
    try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
      String line;
      while ((line = bufferedReader.readLine()) != null) {
        resultStringBuilder.append(line).append("\n");
      }
    }
    return resultStringBuilder.toString();
  }

  private void writerFromOuputStreamWriter(
      OutputStreamWriter outputStreamWriter, String key, List<String> values) throws IOException {
    String valueSave = String.format("%-20s: %s", key, values.toString());
    BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
    bufferedWriter.append(valueSave);
    bufferedWriter.newLine();
    bufferedWriter.close();
  }

  protected List<File> getListFile() {
    return Arrays.asList(new File(this.pathFolder).listFiles());
  }

  protected List<File> getListFileResouce() {
    URL resource = getClass().getClassLoader().getResource(this.pathFolder);
    String url = resource.getPath();
    return Arrays.asList(new File(url).listFiles());
  }

  protected HashMap<String, JSONObject> readJsonFromDataFiles() {
    HashMap<String, JSONObject> jsonObjects = new HashMap<>();
    pathFiles.stream()
        .forEach(
            file -> {
              if (file.isFile())
                try {
                  JSONObject jsonObject =
                      new JSONObject(
                          this.readFromInputStreamReader(
                              this.readFromInputStream(this.readFromPathFile(file))));
                  jsonObjects.put(file.getName(), jsonObject);
                } catch (IOException e) {
                  e.printStackTrace();
                }
            });
    return jsonObjects;
  }

  protected void printValue(JSONObject jsonObject, List<String> values, String keyCheck) {
    Iterator<String> keys = jsonObject.keys();
    while (keys.hasNext()) {
      String key = keys.next();
      if (key.equals(keyCheck)) {
        values.add(jsonObject.getString(key));
      }
      try {
        printValue(jsonObject.getJSONObject(key), values, keyCheck);
      } catch (Exception e) {
      }
    }
  }

  public String getPathFolder() {
    return pathFolder;
  }

  public List<File> getPathFiles() {
    return pathFiles;
  }

  public HashMap<String, JSONObject> getJsonObjects() {
    return jsonObjects;
  }

  public HashMap<String, List<String>> getValueFromKey(String keyCheck) {
    HashMap<String, List<String>> valueFiles = new HashMap<>();
    jsonObjects.forEach(
        (file, jsonObject) -> {
          List<String> values = new ArrayList<>();
          this.printValue(jsonObject, values, keyCheck);
          valueFiles.put(file, values);
        });
    return valueFiles;
  }

  public void writeValueToFile(String fileName, String key, List<String> values) {
    File file = new File(fileName);
    try {
      this.writerFromOuputStreamWriter(
          this.writerFromOuputStream(this.writeFromPathfile(file, true)), key, values);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
