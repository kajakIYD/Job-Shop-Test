package gui;
import java.awt.Color;
import java.awt.Dimension;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYIntervalSeriesCollection;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.plot.XYPlot;
import java.util.concurrent.TimeUnit;
import java.util.Arrays;

/**
* Gantt chart drawer
* @author PF
* @version 1.0
* @since   2018-01-03 
*/
public class XYIntervalBarChart {

int statesCount;

private static final String Task1   = "Task1";
private static final String Task2  = "Task2";
private static final String Task3  = "Task3";
private static final String Task4  = "Task4";
private static final String Task5  = "Task5";

ArrayList<EventStatus> testData = null;
String[] catArray;

JFreeChart chart;
DateAxis dateAxis;

Date chartStartDate;
Date chartEndDate;

ChartPanel chartPanel;

/**
* Gantt chart constructor
* @param Tab array containing information about solution
* @param startDate Start date for gantt chart
*/
public XYIntervalBarChart(int[][] Tab, Date startDate) {
    //super(title);
    // set up some test data
    chartStartDate  = startDate;
    initData(Tab);
    //new Date(1477461600000L);
    //chartEndDate = new Date(1477497600000L);
    //chartEndDate = new Date(1477494000000L);
   
    chart = createIntervalStackedChart();
    chartPanel = new ChartPanel(chart);
    chartPanel.setPreferredSize(new Dimension(700, 200));
    //setContentPane(chartPanel);
    
    
}

/**
* Create interval stacked chart
*/
private JFreeChart createIntervalStackedChart() {
    XYIntervalSeriesCollection dataset = createXYIntervalDataset();
    XYBarRenderer xyRend = new XYBarRenderer();
    xyRend.setShadowVisible(false);
    xyRend.setUseYInterval(true);
    xyRend.setBarPainter(new StandardXYBarPainter());
    xyRend.setSeriesPaint(0, Color.GREEN);
    xyRend.setSeriesPaint(1, Color.CYAN);
    xyRend.setSeriesPaint(2, Color.RED);
    xyRend.setSeriesPaint(3, Color.YELLOW);
    xyRend.setSeriesPaint(4, Color.BLACK);
    xyRend.setSeriesPaint(5, Color.DARK_GRAY);

    dateAxis = new DateAxis();
    dateAxis.setVerticalTickLabels(true);
    dateAxis.setDateFormatOverride(new SimpleDateFormat("dd.MM.yy HH:mm"));
    XYPlot plot = new XYPlot(dataset, new SymbolAxis("", catArray), dateAxis, xyRend);
    plot.setOrientation(PlotOrientation.HORIZONTAL);
    plot.setBackgroundPaint(Color.LIGHT_GRAY);
    return new JFreeChart(plot);
}

/**
* Create XY interval dataset
*/
private XYIntervalSeriesCollection createXYIntervalDataset() {
    XYIntervalSeriesCollection dataset = new XYIntervalSeriesCollection();

    //int statesCount = 5;
    String[] states = new String[] {Task1, Task2, Task3, Task4, Task5};

    XYIntervalSeries[] series = new XYIntervalSeries[statesCount];
    for (int i = 0; i < statesCount; i++) {
        series[i] = new XYIntervalSeries(states[i]);
        dataset.addSeries(series[i]);
    }

    for (int i = 0; i < testData.size(); i++) {
        EventStatus es = testData.get(i);
        int machNo = es.getPlanningNo();
        int state = es.getStatus();
        long eventStart = es.getStartTime();
        long eventEnd = es.getEndTime();
        //long eventEnd = 0;
        /*if (testData.indexOf(es) == testData.size() - 1) {
            eventEnd = chartEndDate.getTime();
        }
        else {
            EventStatus nextEs = testData.get(i + 1);
            if (nextEs.getStartTime() > eventStart) {
                eventEnd = nextEs.getStartTime();
            }
            else {
                eventEnd = chartEndDate.getTime();
            }
        }*/

        long duration = TimeUnit.MILLISECONDS.convert(eventEnd - eventStart, TimeUnit.MILLISECONDS);
        series[state].add(machNo, machNo - 0.2, machNo + 0.2, eventStart, eventStart, eventStart + duration);
    }

    return dataset;
}

/**
* Parse solution data from solution format into gantt-chart format
* @param Tab information from solution
*/
private void initData(int[][] Tab ) {
    String[] Machines = {"Machine1", "Machine2", "Machine3", "Machine4", "Machine5"};
    
    
    testData = new ArrayList<EventStatus>();
    
    long startDate = chartStartDate.getTime();
    long startEvent;
    long endEvent;
    
    // Sprawdzamy liczbę tasków
    int[] array = new int[Tab[1].length];
    for(int i=0; i < Tab[1].length; ++i)
    {
        array[i] = Tab[3][i];
    }
    
    int[] unique = Arrays.stream(array).distinct().toArray();
    statesCount = unique.length;
    
    // Dodajemy taski do listy
    for(int i=0; i < Tab[0].length; i++)
    {
        startEvent = startDate + (Tab[0][i] * 60 * 1000);
        endEvent = startEvent + (Tab[2][i] * 60 * 1000);
        testData.add(new EventStatus(Machines[Tab[1][i]], startEvent , endEvent ,Tab[3][i], Tab[1][i]));
    }
    
    /*testData.add(new EventStatus("Mach-1", 1477468500000L, 1, 0)); // 26.10.16 09:55  standby
    testData.add(new EventStatus("Mach-1", 1477472100000L, 2, 0)); // 26.10.16 10:55  heating
    testData.add(new EventStatus("Mach-1", 1477474200000L, 5, 0)); // 26.10.16 11:30  lowering
    testData.add(new EventStatus("Mach-1", 1477476000000L, 3, 0)); // 26.10.16 12:00  holding
    testData.add(new EventStatus("Mach-1", 1477479600000L, 4, 0)); // 26.10.16 13:00  cooling
    testData.add(new EventStatus("Mach-1", 1477486800000L, 1, 0)); // 26.10.16 15:00  standby

    testData.add(new EventStatus("Mach-2", 1477465200000L, 3, 1)); // 26.10.16 09:00  holding
    testData.add(new EventStatus("Mach-2", 1477472400000L, 2, 1)); // 26.10.16 11:00  heating
    testData.add(new EventStatus("Mach-2", 1477474200000L, 5, 1)); // 26.10.16 11:30  lowering
    testData.add(new EventStatus("Mach-2", 1477476000000L, 2, 1)); // 26.10.16 12:00  heating
    testData.add(new EventStatus("Mach-2", 1477479600000L, 3, 1)); // 26.10.16 13:00  holding
    testData.add(new EventStatus("Mach-2", 1477486800000L, 4, 1)); // 26.10.16 15:00  cooling
    */

    ArrayList<String> list = new ArrayList<>();
    for (EventStatus eventStatus : testData) {
        if (list.contains(eventStatus.getName()))
            continue;
        else
            list.add(eventStatus.getName());
    }

    catArray = new String[list.size()];
    catArray = list.toArray(catArray);
}
    /**
    * Chart object class that hold category, event time and status
    */
    private class EventStatus {

        private String name;
        private long startTime;
        private long endTime;
        private int status;
        private int planningNo;

        public EventStatus(String name, long startTime, long endTime, int status, int planningNo) {
            this.name = name;
            this.startTime = startTime;
            this.endTime = endTime;
            this.status = status;
            this.planningNo = planningNo;
        }

        public int getPlanningNo() {
            return planningNo;
        }
        
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getStartTime() {
            return startTime;
        }
        
        public long getEndTime() {
            return endTime;
        }

        public void setStartTime(long time) {
            this.startTime = time;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }
    }

    /**
    * chartPanel getter
    */
    public ChartPanel GetPanel()
    {
        return chartPanel;
    }
/*public static void main(String[] args) {
    XYIntervalBarChart demo = new XYIntervalBarChart("XYIntervalBarChart");
    demo.pack();
    RefineryUtilities.centerFrameOnScreen(demo);
    demo.setVisible(true);
    }*/
}