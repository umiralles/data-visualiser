package visualiser.datavisualiser.models.Charts;

import javafx.beans.value.ChangeListener;
import javafx.concurrent.Worker;
import javafx.scene.web.WebView;

public interface Chart {
    void setSize(int width, int height);

    ChangeListener<? super Worker.State> showChart(WebView webView, ChangeListener<? super Worker.State> oldListener);
}
