package ottr;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import javafx.stage.Stage;

import java.util.ArrayList;


public class Diagram extends Application {

    // Global Fields
    String fileContent;
    String prefixes;
    ArrayList<HeadModule> headModules;
    BodyModule bodyModules;
    ArrayList<OttrTemplate> templates;
    ArrayList<ClassCoords> classCoords;

    public Diagram(String fileContent) {
        this.fileContent = fileContent;
    }

    // Visualization
    @Override
    public void start(Stage stage) {
        // Window details
        stage.setTitle("OTTR Visualization");
        double screenWidth = 800;
        double screenHeight = 700;

        // Local fields
        ArrayList<Group> classes = new ArrayList<>();
        ArrayList<Line> line = new ArrayList<>();
        classCoords = new ArrayList<>();
        // Starting point of classes
        double startX = 325.0d;
        double startY = 500.0d;

        for (int i = 0; i < templates.size(); i++) {
            // Create main classes (Yellow) - Signatures
            classes.add(createClass(templates.get(i).templateName, startX, startY, 1, i));
            double tempStartY = startY;
            startY = startY - (70 + 17 * templates.get(i).headModule.size());

            // Create father classes (Green) - Instances
            classes.add(createClass(templates.get(i).templateName, startX, startY, 2, i));

            // Child-Father relation - Signature and Pattern
            Line line01 = new Line(startX + 75, startY, startX + 75, tempStartY);
            line01.getStrokeDashArray().addAll(2d);
            line.add(line01);

            // Starting positions of the class files and this can also be random
            if (i % 2 == 0) {
                startX = startX - 200;
            } else {
                startX = startX + 200 + 200;
                startY = startY - 70;
            }

            // Other lines (association relations)
            for (int j = 0; j < templates.get(i).bodyModule.relations01.size(); j++) {
                String startPoint = templates.get(i).bodyModule.relations01.get(j).substring(3);
                double stX = 0;
                double stY = 0;
                String endPoint = templates.get(i).bodyModule.relations02.get(j).substring(3);
                double edX = 0;
                double edY = 0;

                for (ClassCoords classCoord : classCoords) {
                    if (classCoord.className.toLowerCase().trim().contains(startPoint.toLowerCase().trim())) {
                        stX = classCoord.x;
                        stY = classCoord.y;
                    }
                    if (classCoord.className.toLowerCase().trim().contains(endPoint.toLowerCase().trim())) {
                        edX = classCoord.x;
                        edY = classCoord.y;
                    }
                }
                Line lin02 = new Line(stX + 75, stY, edX + 75, edY);
                line.add(lin02);
            }
        }

        Group group = new Group();
        group.getChildren().addAll(line);
        group.getChildren().addAll(classes);
        Scene scene = new Scene(group, screenWidth, screenHeight);
        stage.setScene(scene);
        stage.show();
    }

    // Decorate a Text based on situation
    private Text createText(String string, int type) {
        Text text = new Text(string);
        text.setBoundsType(TextBoundsType.VISUAL);
        if (type == 0) { // Title
            text.setStyle(
                    "-fx-font-family: \"Arial\";" +
                            "-fx-font-size: 18px;"
            );
        } else if (type == 1) { // Other text
            text.setStyle(
                    "-fx-font-family: \"Arial\";" +
                            "-fx-font-size: 13px;"
            );
        } else if (type == 2) { // Instance title with underline
            text.setStyle(
                    "-fx-font-family: \"Arial\";" +
                            "-fx-font-size: 18px;"
            );
            text.setUnderline(true);
        }

        return text;
    }

    // Create a class box
    Group createClass(String className, double startX, double startY, int type, int i) {
        if (type == 0 || type == 2) {
            classCoords.add(new ClassCoords(className, type, startX, startY));
        }
        double classX = 195.0d; // length of a class-box
        double classY = 30.0d;  // height of a class-box head
        double classBodyY = 0;
        Text titleText = createText(className, 0);
        if (type == 2){
            titleText = createText(className, 2);
        }
        double sx = startX + classX / 2 - getSize(titleText, 0);
        double sy = startY + classY / 2 - getSize(titleText, 1);
        titleText.setX(sx);
        titleText.setY(sy);
        String temp = "";
        Color color = null;

        if (type == 1) {
            ArrayList<HeadModule> signatures = templates.get(i).headModule;
            classBodyY = (signatures.size() + 1) * 17.0d;
            temp = signatures.get(0).param.substring(1) + ": (" + signatures.get(0).type + ")";
            for (int j = 1; j < signatures.size(); j++) {
                temp = temp + "\n" + signatures.get(j).param.substring(1) + ": (" + signatures.get(j).type + ")";
                // Add non-blank/optional flags
                if (signatures.get(j).flags.size() != 0) {
                    for (Flag flag : signatures.get(j).flags) {
                        if (flag == Flag.NON_BLANK) {
                            temp = temp + " ⚫";
                        }
                        if (flag == Flag.OPTIONAL) {
                            temp = temp + " ⚪";
                        }
                    }
                }
            }
            color = Color.rgb(255, 255, 153, 1.0);
        } else {
            temp = templates.get(i).bodyModule.args;
            classBodyY = (temp.split("\n").length + 1) * 17.0d; // width of a class-box varies based on number of arguments
            color = Color.rgb(151, 214, 135, 1.0);
        }

        Text bodyText = createText(temp, 1);

        sx = startX + 10;
        sy = startY + classY + 20;
        bodyText.setX(sx);
        bodyText.setY(sy);

        Rectangle rectHead = new Rectangle(startX, startY, classX, classY);
        Rectangle rectBody = new Rectangle(startX, startY + classY + 3, classX, classBodyY);

        rectHead.setFill(color);
        rectBody.setFill(color);
        rectHead.setStyle("-fx-stroke-width: .5; -fx-stroke: #383838;");
        rectBody.setStyle("-fx-stroke-width: .5; -fx-stroke: #383838;");
        return new Group(rectHead, rectBody, titleText, bodyText);
    }

    // returns center of a text
    private double getSize(Text text, int dim) {
        new Scene(new Group(text));
        text.applyCss();
        if (dim == 0) {
            return text.getLayoutBounds().getCenterX();
        } else {
            return text.getLayoutBounds().getCenterY();
        }
    }

    public void parseFile() {
        templates = new ArrayList<>();
        String[] temps = null;
        // Split templates
        temps = fileContent.split("\\.");
        for (String content :
                temps) {
            content = content.trim();
            String signature = "";
            String pattern = "";
            String templateName = "";
            prefixes = "";
            headModules = new ArrayList<>();
            signature = content.split("::")[0];
            pattern = content.split("::")[1];
            pattern = pattern.substring(pattern.indexOf("{") + 1, pattern.indexOf("}")).trim();

            // Set prefixes Wrong --- need to be fixed later
            if (signature.contains("@@")) {
                signature = signature.split("@@")[0];
                prefixes = signature.split("@@")[1];
            }

            // Header: title + flag + param
            String header = "";
            templateName = signature.substring(0, signature.indexOf("["));
            header = signature.substring(signature.indexOf("[") + 1, signature.indexOf("]"));

            int count = header.split(",").length;
            int i = 0;
            String tempBody = pattern;
            while (i < count) {
                ArrayList<Flag> flags = new ArrayList<>();
                String type = "";
                String param = "";
                int len = header.split(",")[i].trim().split(" ").length;
                if (len < 3) {
                    type = header.split(",")[i].trim().split(" ")[0];
                    param = header.split(",")[i].trim().split(" ")[1];
                } else {
                    String sign = header.split(",")[i].trim().split(" ")[0];
                    type = header.split(",")[i].trim().split(" ")[1];
                    param = header.split(",")[i].trim().split(" ")[2];

                    if (sign.contains("?"))
                        flags.add(Flag.OPTIONAL);  //optional
                    if (sign.contains("!"))
                        flags.add(Flag.NON_BLANK);  //non-blank
                }
                headModules.add(new HeadModule(type, param, flags));
                i++;
            }

            boolean flag = false;
            if (pattern.contains(")")) {
                flag = true;
            }
            String args = "";
            ArrayList<String> relations01 = new ArrayList<>();
            ArrayList<String> relations02 = new ArrayList<>();

            // Check the instances
            while (flag) {
                if (tempBody.indexOf(")") == tempBody.length() - 1) {
                    flag = false;
                }
                String instance = tempBody.substring(0, tempBody.indexOf(")") + 1).trim();
                tempBody = tempBody.substring(tempBody.indexOf("),") + 1).trim();

                if (instance.toLowerCase().contains("ottr:triple")) { // Only base template instances
                        args = args + instance.substring(instance.indexOf("(") + 1, instance.indexOf(")")) + "\n";

                } else {
                    String tempStr = instance.substring(0, instance.indexOf("("));
                    if (tempStr.contains(",")) {
                        tempStr = tempStr.replaceAll(",", "");
                    }
                    relations01.add(tempStr);
                    relations02.add(templateName);
                }

            }
            bodyModules = new BodyModule(relations01, relations02, args);
            templates.add(new OttrTemplate(headModules, bodyModules, templateName, prefixes));
        }
    }

    enum Flag {
        NON_BLANK,
        OPTIONAL
    }

    class OttrTemplate {
        ArrayList<HeadModule> headModule;
        BodyModule bodyModule;
        String templateName;
        String prefix;

        public OttrTemplate(ArrayList<HeadModule> headModule, BodyModule bodyModule, String templateName,  String prefix) {
            this.headModule = headModule;
            this.bodyModule = bodyModule;
            this.templateName = templateName;
            this.prefix = prefix;
        }
    }

    class BodyModule {

        ArrayList<String> relations01;
        ArrayList<String> relations02;
        String args;

        public BodyModule(ArrayList<String> relations01, ArrayList<String> relations02, String args) {
            this.relations01 = relations01;
            this.relations02 = relations02;
            this.args = args;

        }
    }

    class HeadModule {
        String type;
        String param;
        String defaultValue;
        ArrayList<Flag> flags;

        public HeadModule(String key, String param, ArrayList<Flag> flags) {
            this.type = key;
            this.param = param;
            this.flags = flags;
        }

        // Constructor with default value
        public HeadModule(String key, String param, ArrayList<Flag> flags, String defaultValue) {
            this.type = key;
            this.param = param;
            this.flags = flags;
            this.defaultValue = defaultValue;
        }

    }

    class ClassCoords {
        String className;
        int type;
        double x;
        double y;

        public ClassCoords(String className, int type, double x, double y) {
            this.className = className;
            this.type = type;
            this.x = x;
            this.y = y;
        }
    }

}

