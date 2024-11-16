import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Picture;
import android.graphics.drawable.PictureDrawable;
import com.caverock.androidsvg.SVG;

import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.IOException;
import java.io.InputStream;

import com.caverock.androidsvg.SVGParseException;


/** Demo app chooser which allows you pick from all available testing Activities. */

  private static final String STATE_SELECTED_POSE = "selected_pose";

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);

    // Locate the views
    TextView englishNameTextView = findViewById(R.id.tvEnglishName);
    TextView sanskritNameTextView = findViewById(R.id.tvSanskritName);
    ImageView poseImageView = findViewById(R.id.ivPoseImage);

    try {
      String jsonString = loadJSONFromAsset();
      JSONObject jsonObject = new JSONObject(jsonString);
      JSONArray posesArray = jsonObject.getJSONArray("Poses");

      for (int i = 0; i < posesArray.length(); i++) {
        JSONObject poseObject = posesArray.getJSONObject(i);
          String sanskritName = poseObject.getString("sanskrit_name");

          // Set data to views
          englishNameTextView.setText(englishName);
          sanskritNameTextView.setText(sanskritName);
          loadSVGFromAssets(imageUrl, poseImageView);

          break;
        }
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  private String loadJSONFromAsset() {
    String json = null;
    try {
      InputStream is = getAssets().open("pose/Poses.json");
      int size = is.available();
      byte[] buffer = new byte[size];
      is.read(buffer);
      is.close();
      json = new String(buffer, "UTF-8");
    } catch (IOException ex) {
      ex.printStackTrace();
      // fall through
    }
    return json;
  }

  private void loadSVGFromAssets(String fileName, ImageView imageView) {
    try (InputStream inputStream = getAssets().open(fileName)) {
      // Load the SVG from the InputStream
      SVG svg = SVG.getFromInputStream(inputStream);

      // Convert SVG to a Picture and wrap it in a PictureDrawable
      Picture picture = svg.renderToPicture();
      PictureDrawable drawable = new PictureDrawable(picture);

      // Set the drawable to the ImageView
      imageView.setLayerType(ImageView.LAYER_TYPE_SOFTWARE, null);
      imageView.setImageDrawable(drawable);
      // the following does not work
//      imageView.setColorFilter(getResources().getColor(android.R.color.black), PorterDuff.Mode.SRC_IN);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (SVGParseException s){
      s.printStackTrace();
    }
  }
}
