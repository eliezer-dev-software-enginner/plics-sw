package my_app.updater;

import megalodonte.application.Context;
import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.components.Text;
import megalodonte.components.layout_components.Container;
import megalodonte.props.ContainerProps;
import megalodonte.props.TextProps;

public class HomeScreen implements ScreenComponent {
    private final HomeScreenViewModel viewModel;

    public HomeScreen(Context ctx) {
        this.viewModel = new HomeScreenViewModel();
    }

    @Override
    public void onMount() {
        viewModel.update();
    }

    public Component render() {
        return new Container(new ContainerProps().paddingAll(10))
                .children(
                    new Text(viewModel.updateStatus, new TextProps().fontSize(25))
                );
    }
}
