package net.yura.domination.ios;

import net.yura.domination.engine.core.Player;
import net.yura.domination.engine.core.RiskGame;
import net.yura.domination.engine.core.StatType;
import net.yura.domination.engine.translation.TranslationBundle;
import net.yura.domination.mobile.PicturePanel;
import net.yura.domination.mobile.flashgui.DominationMain;
import net.yura.mobile.gui.Application;
import apple.foundation.NSArray;
import apple.foundation.NSMutableArray;
import apple.foundation.NSNumber;
import apple.uikit.UIColor;
import apple.uikit.UIFont;
import org.moe.natj.general.Pointer;
import org.moe.natj.general.ann.NFloat;
import org.moe.natj.general.ann.Owned;
import org.moe.natj.general.ann.RegisterOnStartup;
import org.moe.natj.objc.ObjCRuntime;
import org.moe.natj.objc.ann.IBOutlet;
import org.moe.natj.objc.ann.ObjCClassName;
import org.moe.natj.objc.ann.Property;
import org.moe.natj.objc.ann.Selector;
import apple.uikit.UIScreen;
import apple.uikit.UIViewController;
import org.moe.samples.simplechart.charts.ChartDataEntry;
import org.moe.samples.simplechart.charts.ChartViewBase;
import org.moe.samples.simplechart.charts.ChartXAxis;
import org.moe.samples.simplechart.charts.ChartYAxis;
import org.moe.samples.simplechart.charts.LineChartData;
import org.moe.samples.simplechart.charts.LineChartDataSet;
import org.moe.samples.simplechart.charts.LineChartView;
import org.moe.samples.simplechart.charts.enums.XAxisLabelPosition;
import org.moe.samples.simplechart.charts.protocol.ChartViewDelegate;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

@org.moe.natj.general.ann.Runtime(ObjCRuntime.class)
@ObjCClassName("StatsViewController")
//@RegisterOnStartup
public class StatsViewController extends UIViewController implements ChartViewDelegate {

    private final ResourceBundle resb = TranslationBundle.getBundle();

    private LineChartView lineChartView;

    @Owned
    @Selector("alloc")
    public static native StatsViewController alloc();

    @Selector("init")
    public native StatsViewController init();

    protected StatsViewController(Pointer peer) {
        super(peer);
    }

    public static List<Player> getPlayersStats() {
        DominationMain dmain = (DominationMain) Application.getInstance();
        RiskGame game = dmain.risk.getGame();
        // if we open the stats activity at the same time as closing the game, avoid throwing a error
        return game == null ? Collections.EMPTY_LIST : game.getPlayersStats();
    }

    @Override
    public void viewWillAppear(boolean animated) {
        super.viewWillAppear(animated);
        navigationController().setNavigationBarHidden(false);
    }

    @Property
    @IBOutlet
    @Selector("lineChartView")
    public LineChartView getLineChartView() {
        return lineChartView;
    }

    @Selector("setLineChartView:")
    public void setLineChartView(LineChartView lineChartView) {
        this.lineChartView = lineChartView;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void viewDidLoad() {
        super.viewDidLoad();

        setLineChartView(LineChartView.alloc().initWithFrame(UIScreen.mainScreen().bounds()));

        final LineChartView chartView = getLineChartView();
        chartView.setDelegate(this);
        view().addSubview(chartView);

        chartView.legend().setTextColor(UIColor.whiteColor());
        chartView.setDragEnabled(true);
        chartView.setScaleEnabled(true);
        chartView.setPinchZoomEnabled(true);
        chartView.setDrawGridBackgroundEnabled(false);

        ChartXAxis xAxis = chartView.xAxis();
        xAxis.setGridLineDashLengths(arrayOfFloats(10.0f, 10.0f));
        xAxis.setGridLineDashPhase(0f);
        xAxis.setLabelTextColor(UIColor.whiteColor());
        xAxis.setLabelPosition(XAxisLabelPosition.Bottom);
        xAxis.setAxisMinimum(0d);
        xAxis.setGranularity(1D);
        xAxis.setLabelCount(25);

        ChartYAxis yAxis = chartView.leftAxis();
        yAxis.setAxisMinimum(0.0);
        yAxis.setGridLineDashLengths(arrayOfFloats(5.0f, 5.0f));
        yAxis.setDrawZeroLineEnabled(false);
        yAxis.setDrawLimitLinesBehindDataEnabled(true);
        yAxis.setLabelTextColor(UIColor.whiteColor());
        yAxis.setGranularity(1D);
        yAxis.setLabelCount(25);

        chartView.rightAxis().setEnabled(false);
        chartView.setAutoScaleMinMaxEnabled(true);

        setData(StatType.ARMIES);
    }

    private void setData(StatType statType) {
        setTitle(resb.getString("swing.tab.statistics") + " - " + resb.getString("swing.toolbar." + statType.getName()));

        List<Player> players = getPlayersStats();
        NSMutableArray<LineChartDataSet> dataSets = (NSMutableArray<LineChartDataSet>)NSMutableArray.arrayWithCapacity(players.size());
        for (Player player : players) {
            dataSets.add(getLineChartDataSet(player, statType));
        }
        LineChartData data = LineChartData.alloc().initWithDataSets(dataSets);
        getLineChartView().setData(data);
    }

    private LineChartDataSet getLineChartDataSet(Player player, StatType statType) {

        UIColor playerColor = Graphics.getColor(player.getColor());
        double[] stats = player.getStatistics(statType);

        Image img = PicturePanel.getIconForColor(player.getColor());
        // TODO we need to scale this image

        NSMutableArray<ChartDataEntry> values = (NSMutableArray<ChartDataEntry>)NSMutableArray.arrayWithCapacity(stats.length);
        for (int i = 0; i < stats.length; ++i) {
            ChartDataEntry entry = ChartDataEntry.alloc().initWithXY(i + 1, stats[i]);
            if (img != null) {
                entry.setIcon(img.getUIImage());
            }
            values.add(entry);
        }

        LineChartDataSet set1 = LineChartDataSet.alloc().initWithEntriesLabel(values, player.getName());

        set1.setHighlightLineDashLengths(arrayOfFloats(5.0f, 2.5f));
        set1.setColor(playerColor);
        set1.setCircleColor(playerColor);
        set1.setLineWidth(1.0);
        set1.setCircleRadius(3.0);
        set1.setDrawCircleHoleEnabled(false);
        set1.setValueFont(UIFont.systemFontOfSize(9.f));
        set1.setDrawIconsEnabled(img != null);
        set1.setDrawValuesEnabled(false);

        //set1.setFormLineDashLengths(arrayOfFloats(5.0f, 2.5f));
        set1.setFormLineWidth(1.0);
        set1.setFormSize(15.0);

        return set1;
    }

    @SuppressWarnings("unchecked")
    public static NSArray<NSNumber> arrayOfFloats(float... values) {
        NSMutableArray array = NSMutableArray.alloc().initWithCapacity(values.length);
        for (float value : values) {
            array.add(NSNumber.alloc().initWithFloat(value));
        }
        return array;
    }

    @Override
    public void chartTranslatedDXDY(ChartViewBase chartView, @NFloat double dX, @NFloat double dY) {
        System.out.println("translated -> " + dX + ":" + dY);
    }
}
