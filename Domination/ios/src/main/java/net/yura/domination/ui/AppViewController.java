package net.yura.domination.ui;

import net.yura.domination.engine.core.RiskGame;
import net.yura.domination.mobile.flashgui.DominationMain;

import org.moe.natj.general.Pointer;
import org.moe.natj.general.ann.Owned;
import org.moe.natj.general.ann.RegisterOnStartup;
import org.moe.natj.objc.ObjCRuntime;
import org.moe.natj.objc.ann.ObjCClassName;
import org.moe.natj.objc.ann.Property;
import org.moe.natj.objc.ann.Selector;

import apple.NSObject;
import apple.uikit.NSLayoutConstraint;
import apple.uikit.UIButton;
import apple.uikit.UIColor;
import apple.uikit.UILabel;
import apple.uikit.UIViewController;
import apple.uikit.enums.NSLayoutAttribute;
import apple.uikit.enums.NSLayoutRelation;
import apple.uikit.enums.UIViewContentMode;

@org.moe.natj.general.ann.Runtime(ObjCRuntime.class)
@ObjCClassName("AppViewController")
@RegisterOnStartup
public class AppViewController extends UIViewController {

    private CustomView customView;

    @Owned
    @Selector("alloc")
    public static native AppViewController alloc();

    @Selector("init")
    public native AppViewController init();

    protected AppViewController(Pointer peer) {
        super(peer);
    }

    @Override
    public void viewDidLoad() {
        super.viewDidLoad();


        customView = CustomView.alloc().init();

        customView.setBackgroundColor(UIColor.clearColor());
        customView.setTranslatesAutoresizingMaskIntoConstraints(false);
        customView.setContentMode(UIViewContentMode.Redraw);

        view().addSubview(customView);
        view().setNeedsLayout();

        NSLayoutConstraint constraint;
        constraint = NSLayoutConstraint.constraintWithItemAttributeRelatedByToItemAttributeMultiplierConstant(
                view(), NSLayoutAttribute.Left, NSLayoutRelation.Equal, customView, NSLayoutAttribute.Left, 1.0, 0);
        view().addConstraint(constraint);
        constraint = NSLayoutConstraint.constraintWithItemAttributeRelatedByToItemAttributeMultiplierConstant(
                view(), NSLayoutAttribute.Right, NSLayoutRelation.Equal, customView, NSLayoutAttribute.Right, 1.0, 0);
        view().addConstraint(constraint);
        constraint = NSLayoutConstraint.constraintWithItemAttributeRelatedByToItemAttributeMultiplierConstant(
                view(), NSLayoutAttribute.Top, NSLayoutRelation.Equal, customView, NSLayoutAttribute.Top, 1.0, 0);
        view().addConstraint(constraint);
        constraint = NSLayoutConstraint.constraintWithItemAttributeRelatedByToItemAttributeMultiplierConstant(
                view(), NSLayoutAttribute.Bottom, NSLayoutRelation.Equal, customView, NSLayoutAttribute.Bottom, 1.0, 0);
        view().addConstraint(constraint);


        DominationMain main = new DominationMain();
    }
}
