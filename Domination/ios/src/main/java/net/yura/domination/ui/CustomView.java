package net.yura.domination.ui;


import org.moe.natj.general.NatJ;
import org.moe.natj.general.Pointer;
import org.moe.natj.general.ann.ByValue;
import org.moe.natj.general.ann.Generated;
import org.moe.natj.general.ann.Owned;
import org.moe.natj.general.ann.RegisterOnStartup;
import org.moe.natj.objc.ObjCRuntime;
import org.moe.natj.objc.ann.ObjCClassName;
import org.moe.natj.objc.ann.Selector;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.lcdui.Graphics;

import apple.coregraphics.opaque.CGContextRef;
import apple.coregraphics.struct.CGPoint;
import apple.coregraphics.struct.CGRect;
import apple.coregraphics.struct.CGSize;
import apple.uikit.UIColor;
import apple.uikit.UIImage;
import apple.uikit.UIView;
import apple.uikit.c.UIKit;

import static apple.coregraphics.c.CoreGraphics.CGContextAddArc;
import static apple.coregraphics.c.CoreGraphics.CGContextAddEllipseInRect;
import static apple.coregraphics.c.CoreGraphics.CGContextAddLineToPoint;
import static apple.coregraphics.c.CoreGraphics.CGContextAddRect;
import static apple.coregraphics.c.CoreGraphics.CGContextBeginPath;
import static apple.coregraphics.c.CoreGraphics.CGContextClosePath;
import static apple.coregraphics.c.CoreGraphics.CGContextDrawImage;
import static apple.coregraphics.c.CoreGraphics.CGContextFillPath;
import static apple.coregraphics.c.CoreGraphics.CGContextMoveToPoint;
import static apple.coregraphics.c.CoreGraphics.CGContextRestoreGState;
import static apple.coregraphics.c.CoreGraphics.CGContextSaveGState;
import static apple.coregraphics.c.CoreGraphics.CGContextScaleCTM;
import static apple.coregraphics.c.CoreGraphics.CGContextSetFillColor;
import static apple.coregraphics.c.CoreGraphics.CGContextSetFillColorWithColor;
import static apple.coregraphics.c.CoreGraphics.CGContextSetLineWidth;
import static apple.coregraphics.c.CoreGraphics.CGContextSetStrokeColorWithColor;
import static apple.coregraphics.c.CoreGraphics.CGContextStrokePath;
import static apple.coregraphics.c.CoreGraphics.CGContextTranslateCTM;
import static apple.uikit.c.UIKit.UIGraphicsBeginImageContext;
import static apple.uikit.c.UIKit.UIGraphicsEndImageContext;
import static apple.uikit.c.UIKit.UIGraphicsPopContext;
import static apple.uikit.c.UIKit.UIGraphicsPushContext;

@Generated
@org.moe.natj.general.ann.Runtime(ObjCRuntime.class)
@ObjCClassName("ImageWithPlateView")
@RegisterOnStartup
public class CustomView extends UIView {

    static {
        NatJ.register();
    }

    @Generated
    protected CustomView(Pointer peer) {
        super(peer);
    }

    @Generated
    @Owned
    @Selector("alloc")
    public static native CustomView alloc();

    @Generated
    @Selector("init")
    public native CustomView init();

    @Generated
    @Selector("initWithFrame:")
    public native CustomView initWithFrame(@ByValue CGRect frame);

    public static void moveTo(Coordinate c, CGContextRef context) {
        double x = c.getX();
        double y = c.getY();
        CGContextMoveToPoint(context, x, y);
    }

    public static void lineTo(Coordinate c, CGContextRef context) {
        double x = c.getX();
        double y = c.getY();
        CGContextAddLineToPoint(context, x, y);
    }

    @Override
    public void drawRect(CGRect rect) {

        double centerX = bounds().size().width() / 2;
        double space = 10;
        double leftX = centerX - 50 - space;
        double rightX = centerX + 50 + space;

        drawRect(leftX - 50, 50, 100, 100, UIColor.blueColor(), true);

        drawRect(rightX - 50, 50, 100, 100, UIColor.blueColor(), false);

        drawCircle(leftX, 210, 50, UIColor.greenColor(), true);

        drawCircle(rightX, 210, 50, UIColor.greenColor(), false);

        UIImage logo1 = UIImage.alloc().imageNamed("marble.jpg");

        if (logo1 != null) {
            drawImage(centerX - 40, 270, 80, 50, logo1);
        }

        List<Coordinate> points = new ArrayList<Coordinate>();
        points.add(new Coordinate(leftX, 270));
        points.add(new Coordinate(leftX + 50, 350));
        points.add(new Coordinate(leftX - 50, 350));

        drawLines(points, UIColor.redColor(), true);

        points.clear();
        points.add(new Coordinate(rightX, 270));
        points.add(new Coordinate(rightX + 50, 350));
        points.add(new Coordinate(rightX - 50, 350));

        drawLines(points, UIColor.redColor(), false);

        drawEllipseInRect(leftX - 50, 360, 100, 50, UIColor.orangeColor(), true);

        drawEllipseInRect(rightX - 50, 360, 100, 50, UIColor.orangeColor(), false);

        UIImage logo2 = UIImage.alloc().imageNamed("carhunt_migeran_logo.png");
        if (logo2 != null) {
            drawImage(centerX - 75, 400, 150, 100, logo2);
        }

        CGContextRef context = UIKit.UIGraphicsGetCurrentContext();
        Graphics g = new Graphics(context);

    }

    private void drawLines(List<Coordinate> points, UIColor color, boolean fill) {

        CGContextRef context = UIKit.UIGraphicsGetCurrentContext();

        // Draw the polygon
        if (points != null && points.size() > 0) {
            CGContextSetLineWidth(context, 2.0);
            moveTo(points.get(0), context);
            for (int i = 1; i < points.size(); ++i) {
                lineTo(points.get(i), context);
            }
            CGContextClosePath(context);

            if (!fill) {
                CGContextSetStrokeColorWithColor(context, color.CGColor());
                CGContextStrokePath(context);
            } else {
                CGContextSetFillColorWithColor(context, color.CGColor());
                CGContextFillPath(context);
            }
        }
    }

    private void drawCircle(double x, double y, double radius, UIColor color, boolean fill) {
        CGContextRef ctx = UIKit.UIGraphicsGetCurrentContext();
        CGContextBeginPath(ctx);

        CGContextSetLineWidth(ctx, 2.0);
        CGContextAddArc(ctx, x, y, radius, 0, 2 * Math.PI, 0);

        if (!fill) {
            CGContextSetStrokeColorWithColor(ctx, color.CGColor());
            CGContextStrokePath(ctx);
        } else {
            CGContextSetFillColorWithColor(ctx, color.CGColor());
            CGContextFillPath(ctx);
        }
    }

    private void drawRect(double x, double y, double width, double height, UIColor color, boolean fill) {
        CGContextRef ctx = UIKit.UIGraphicsGetCurrentContext();
        CGRect rectangle = new CGRect(new CGPoint(x, y), new CGSize(width, height));
        CGContextAddRect(ctx, rectangle);

        if (!fill) {
            CGContextSetStrokeColorWithColor(ctx, color.CGColor());
            CGContextStrokePath(ctx);
        } else {
            CGContextSetFillColorWithColor(ctx, color.CGColor());
            CGContextFillPath(ctx);
        }
    }

    private void drawEllipseInRect(double x, double y, double width, double height, UIColor color, boolean fill) {
        CGContextRef ctx = UIKit.UIGraphicsGetCurrentContext();
        CGRect rectangle = new CGRect(new CGPoint(x, y), new CGSize(width, height));
        CGContextAddEllipseInRect(ctx, rectangle);

        if (!fill) {
            CGContextSetStrokeColorWithColor(ctx, color.CGColor());
            CGContextStrokePath(ctx);
        } else {
            CGContextSetFillColorWithColor(ctx, color.CGColor());
            CGContextFillPath(ctx);
        }
    }

    private void drawImage(double x, double y, double width, double height, UIImage image) {
        CGContextRef context = UIKit.UIGraphicsGetCurrentContext();
        CGRect imageRect = new CGRect(new CGPoint(x, y), new CGSize(width, height));

        UIGraphicsBeginImageContext(imageRect.size());

        UIGraphicsPushContext(context);

        image.drawInRect(imageRect);

        UIGraphicsPopContext();

        UIGraphicsEndImageContext();
    }

}