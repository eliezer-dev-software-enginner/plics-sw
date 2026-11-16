package plics_sw_new;


import megalodonte.base.components.Component;
import megalodonte.base.components.ScreenComponent;
import megalodonte.components.Text;
import megalodonte.components.layout_components.Container;
import megalodonte.props.ContainerProps;
import megalodonte.props.TextProps;


public final class HomeScreen implements ScreenComponent {

@Override public Component render() {

    return new Container(new ContainerProps().paddingAll(28)).children(

            new Text("plics-sw-new", new TextProps().fontSize(28)));

}
}
