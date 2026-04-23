import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class Main extends JFrame {

    private final ArrayList<Point> controlPoints = new ArrayList<>();
    private final DefaultListModel<String> pointListModel = new DefaultListModel<>();
    private final JList<String> pointList;
    private final JTextField xField = new JTextField(5);
    private final JTextField yField = new JTextField(5);
    private Point selectedPoint = null;

    public Main() {
        setTitle("Bézier Curve Editor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout());

        JPanel canvas = getjPanel();
        // canvas.setBackground(Color.BLUE);
        add(canvas, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());

        pointList = new JList<>(pointListModel);
        pointList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && pointList.getSelectedIndex() != -1) {
                selectedPoint = controlPoints.get(pointList.getSelectedIndex());
                xField.setText(String.valueOf(selectedPoint.x));
                yField.setText(String.valueOf(selectedPoint.y));
            }
        });

        JScrollPane scrollPane = new JScrollPane(pointList);
        controlPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("X:"));
        inputPanel.add(xField);
        inputPanel.add(new JLabel("Y:"));
        inputPanel.add(yField);
        JButton updateButton = new JButton("Update Point");
        updateButton.addActionListener(e -> updateSelectedPoint());
        inputPanel.add(updateButton);

        controlPanel.add(inputPanel, BorderLayout.SOUTH);

        add(controlPanel, BorderLayout.EAST);
    }

    private JPanel getjPanel() {
        JPanel canvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBezierCurve(g);
                drawControlPoints(g);
            }
        };
        canvas.setBackground(Color.WHITE);
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                addOrSelectPoint(e.getPoint());
                repaint();
            }
        });
        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (selectedPoint != null) {
                    selectedPoint.setLocation(e.getPoint());
                    updatePointList();
                    repaint();
                }
            }
        });
        return canvas;
    }

    private void addOrSelectPoint(Point point) {
        boolean pointExists = false;
        for (Point p : controlPoints) {
            if (p.distance(point) < 10) {
                selectedPoint = p;
                pointExists = true;
                break;
            }
        }
        if (!pointExists) {
            controlPoints.add(point);
            updatePointList();
        }
    }

    private void updateSelectedPoint() {
        try {
            int x = Integer.parseInt(xField.getText());
            int y = Integer.parseInt(yField.getText());
            if (selectedPoint != null) {
                selectedPoint.setLocation(x, y);
            } else {
                Point newPoint = new Point(x, y);
                controlPoints.add(newPoint);
                selectedPoint = newPoint;
            }
            updatePointList();
            repaint();
            selectedPoint = null;
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid coordinates", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void updatePointList() {
        pointListModel.clear();
        for (Point p : controlPoints) {
            pointListModel.addElement("(" + p.x + ", " + p.y + ")");
        }
    }

    private void drawControlPoints(Graphics g) {
        g.setColor(Color.RED);
        for (Point p : controlPoints) {
            g.fillOval(p.x - 5, p.y - 5, 10, 10);
        }
    }

    private void drawBezierCurve(Graphics g) {
        if (controlPoints.size() < 2) return;

        g.setColor(Color.BLUE);

        Point previousPoint = calculateBezierPoint(0, controlPoints);
        for (double t = 0.01; t <= 1; t += 0.01) {
            Point currentPoint = calculateBezierPoint(t, controlPoints);
            g.drawLine(previousPoint.x, previousPoint.y, currentPoint.x, currentPoint.y);
            previousPoint = currentPoint;
        }
    }


    private Point calculateBezierPoint(double t, ArrayList<Point> points) {
        int n = points.size() - 1;
        double x = 0, y = 0;
        for (int i = 0; i <= n; i++) {
            double binomialCoefficient = binomialCoefficient(n, i);
            double factor = binomialCoefficient * Math.pow(1 - t, n - i) * Math.pow(t, i);
            x += factor * points.get(i).x;
            y += factor * points.get(i).y;
        }
        return new Point((int) x, (int) y);
    }

    private double binomialCoefficient(int n, int k) {
        double result = 1;
        for (int i = 1; i <= k; i++) {
            result *= (n - (k - i)) / (double) i;
        }
        return result;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Main editor = new Main();
            editor.setVisible(true);
        });
    }
}
