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

    // Constructor (text of the OTTR template is feed to the Parser here)
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
            // Create main classes (Yellow)
            classes.add( createClass(templates.get(i).templateName, startX ,startY , 1, i));
            double tempStartY = startY;
            startY = startY - (70+17*templates.get(i).headModule.size());

            // Create father classes (Green)
            classes.add( createClass(templates.get(i).fatherClassTitle, startX ,startY , 0, i));

            // Child-Father relation added here such as inheritance
            Line line01 = new Line(startX + 75, startY, startX + 75, tempStartY);
            line01.getStrokeDashArray().addAll(2d);
            line.add(line01);

            // Starting postions of the class files and this can also be random
            if(i%2==0){
                startX = startX - 200;
            }else{
                startX = startX + 200 + 200;
                startY = startY - 70;
            }

            // Other lines (association relations) are added here
            for (int j = 0; j < templates.get(i).bodyModule.relations01.size(); j++) {
                String startPoint = templates.get(i).bodyModule.relations01.get(j).substring(3);
                double stX = 0;
                double stY = 0;
                String endPoint = templates.get(i).bodyModule.relations02.get(j);
                double edX = 0;
                double edY = 0;

                for (int k = 0; k < classCoords.size(); k++) {
                    if(classCoords.get(k).className.toLowerCase().trim().contains(startPoint.toLowerCase().trim())){
                        stX = classCoords.get(k).x;
                        stY = classCoords.get(k).y;
                    }
                    if(classCoords.get(k).className.toLowerCase().trim().contains(endPoint.toLowerCase().trim())){
                        edX = classCoords.get(k).x;
                        edY = classCoords.get(k).y;
                    }
                }
                Line lin02 = new Line(stX + 75, stY, edX+ 75, edY);
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
        if(type==0) {
            text.setStyle(
                    "-fx-font-family: \"Arial\";" +
                            "-fx-font-size: 18px;"
            );
        }else if(type==1){
            text.setStyle(
                    "-fx-font-family: \"Arial\";" +
                            "-fx-font-size: 12px;"
            );
        }

        return text;
    }

    // Create a class box
    Group createClass(String className, double startX, double startY, int type, int i){
        if(type ==0 ){
            classCoords.add(new ClassCoords(className,type,startX ,startY ));
        }
        double classX = 190.0d; // length of a class-box
        double classY = 30.0d;  // height of a class-box head
        double classBodyY = 0;
        Text titleText = createText(className,0);
        double sx = startX+classX/2- getSize(titleText,0);
        double sy = startY+classY/2- getSize(titleText,1);
        titleText.setX(sx);
        titleText.setY(sy);
        String temp = "";
        Color color = null;

        if(type == 0){
            temp = templates.get(i).bodyModule.features;
            classBodyY = (temp.split("\n").length + 1) * 17.0d; // width of a class-box varies based on number of features
            color = Color.rgb(151, 214, 135, 1.0);
        }else {
            ArrayList<HeadModule> features = templates.get(i).headModule;
            classBodyY = (features.size() + 1)  * 17.0d;
            temp = features.get(0).value.substring(1) + ": (" + features.get(0).key + ")";
            for (int j =1; j<features.size();j++) {
                temp = temp + "\n" + features.get(j).value.substring(1) + ": (" + features.get(j).key + ")";
            }
            color = Color.rgb(255, 255, 153, 1.0);
        }

        Text bodyText = createText(temp,1);
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
    private double getSize(Text text,int dim) {
        new Scene(new Group(text));
        text.applyCss();
        if(dim==0){
            return text.getLayoutBounds().getCenterX();
        }else{
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
            String fatherClassTitle = "";
            prefixes = "";
            headModules = new ArrayList<>();
            signature = content.split("::")[0];
            pattern = content.split("::")[1];
            pattern = pattern.substring(pattern.indexOf("{")+1,pattern.indexOf("}")).trim();

            // Set prefixes Wrong --- need to be fixed
            if(signature.contains("@@")){
                signature = signature.split("@@")[0];
                prefixes = signature.split("@@")[1];
            }

            String header = "";
            if(signature.contains("(")){
                templateName = signature.substring(0,signature.indexOf("("));
                header = signature.substring(signature.indexOf("(")+1,signature.indexOf(")"));
            }else{
                templateName = signature.substring(0,signature.indexOf("["));
                header = signature.substring(signature.indexOf("[")+1,signature.indexOf("]"));
            }
            if(templateName.contains(":")){
                templateName = templateName.split(":")[1];
            }

            int count = header.split(",").length;
            int i = 0;
            String tempBody = pattern;
            while (i<count) {
                boolean[] conditions = new boolean[3];
                String key = "";
                String value = "";
                int len = header.split(",")[i].trim().split(" ").length;
                if(len<3){
                    key = header.split(",")[i].trim().split(" ")[0];
                    value = header.split(",")[i].trim().split(" ")[1];
                }else {
                    String sign = header.split(",")[i].trim().split(" ")[0];
                    key = header.split(",")[i].trim().split(" ")[1];
                    value = header.split(",")[i].trim().split(" ")[2];

                    if(sign.equals("?"))
                        conditions[0] = true;  //optional
                    if(sign.equals("+"))
                        conditions[1] = true;  //blank allowed
                    if(sign.equals("!"))
                        conditions[2] = true;  //default value
                }
                headModules.add(new HeadModule(key,value,conditions));
                i++;
            }

            boolean flag = false;
            if(pattern.contains(")")) {
                flag = true;
            }
            String features = "";
            ArrayList<String> relations01 = new ArrayList<>();
            ArrayList<String> relations02 = new ArrayList<>();

            while (flag){
                if(tempBody.indexOf(")")==tempBody.length()-1){
                    flag = false;
                }
                String properties = tempBody.substring(0,tempBody.indexOf(")")+1).trim();
                tempBody = tempBody.substring(tempBody.indexOf("),")+1).trim();

                if(properties.toLowerCase().contains("ottr:triple")){
                    if(properties.toLowerCase().contains("type")){
                        fatherClassTitle = properties.substring(properties.lastIndexOf(" "),properties.indexOf(")"));
                    }else{
                        features = features + properties.substring(properties.indexOf("(") + 1, properties.indexOf(")")) +"\n";
                    }
                }else {
                    String tempStr = properties.substring(0, properties.indexOf("("));
                    if (tempStr.contains(",")) {
                        tempStr = tempStr.replaceAll(",", "");
                    }
                    relations01.add(tempStr);
                    relations02.add(templateName);
                }

            }
            bodyModules = new BodyModule(relations01, relations02, features);
            templates.add( new OttrTemplate(headModules,bodyModules,templateName,fatherClassTitle,prefixes));
        }
    }

    // inner class
    class OttrTemplate
    {
        ArrayList<HeadModule> headModule;
        BodyModule bodyModule;
        String templateName;
        String fatherClassTitle;
        String prefix;

        public OttrTemplate(ArrayList<HeadModule> headModule, BodyModule bodyModule, String templateName, String fatherClassTitle, String prefix) {
            this.headModule = headModule;
            this.bodyModule = bodyModule;
            this.templateName = templateName;
            this.fatherClassTitle = fatherClassTitle;
            this.prefix = prefix;
        }
    }

    // inner class
    class BodyModule{

        ArrayList<String> relations01;
        ArrayList<String> relations02;
        String features;

        public BodyModule(ArrayList<String> relations01, ArrayList<String> relations02, String features) {
            this.relations01 = relations01;
            this.relations02 = relations02;
            this.features = features;

        }
    }

    // inner class
    class HeadModule{
        // fields
        String key;
        String value;
        String defaultValue;
        boolean[] conditions;

        // Constructor
        public HeadModule(String key, String value, boolean[] conditions) {
            this.key = key;
            this.value = value;
            this.conditions = conditions;
        }

        // Constructor with default value
        public HeadModule(String key, String value, boolean[] conditions, String defaultValue) {
            this.key = key;
            this.value = value;
            this.conditions = conditions;
            this.defaultValue = defaultValue;
        }

    }

    //Inner class
    class ClassCoords{
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

