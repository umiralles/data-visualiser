package visualiser.datavisualiser.models.Charts;

import javafx.scene.web.WebView;

public interface Chart {
    void setSize(int width, int height);

    void showChart(WebView webView);
}
