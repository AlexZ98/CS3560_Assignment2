import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.util.*;

public class GUIDriver extends Application {
    //Main driver class which supports a main method that runs the entire program and the GUI
    public static void main(String[] args) {
        launch(args);

    }

    @Override
    public void start(Stage primaryStage) {
        List<User> users = new ArrayList<>();
        List<UserGroup> groups = new ArrayList<>();
        List<String> userIds = new ArrayList<>();
        List<String> groupIds = new ArrayList<>();
        Map<User, Long> map = new HashMap<>();

        AdminPanel adminPanel = AdminPanel.getInstance();
        adminPanel.getRoot().setExpanded(true);
        TreeView<UserInterface> treeView = new TreeView<>(AdminPanel.getRoot());

        StackPane treeContainer = new StackPane();
        treeContainer.getChildren().add(treeView);

        HBox hb1 = new HBox(15,adminPanel.getUserIdLabel(), adminPanel.getUserId());
        HBox hb2 = new HBox(15, adminPanel.getUserGroupLabel(), adminPanel.getGroupId(), adminPanel.getGroupCreationTime());
        HBox hb3 = new HBox(15, adminPanel.getAddUser(), adminPanel.getAddGroup());
        HBox hb4 = new HBox(15, adminPanel.getShowGroupTotal(), adminPanel.getShowPosPercent());
        HBox hb5 = new HBox(15,adminPanel.getShowUserTotal(), adminPanel.getShowMsgTotal());
        HBox hb6 = new HBox(15, adminPanel.getOpenUserView());
        HBox hb7 = new HBox(adminPanel.getIdVerification());
        HBox hb8 = new HBox(15, adminPanel.getRecentUpdate());
        VBox vbox = new VBox(20, hb1, hb2, hb3, hb4, hb5, hb6, hb7, hb8);
        vbox.setAlignment(Pos.TOP_CENTER);
        vbox.setPadding(new Insets(5));

        adminPanel.setVgap(5);
        adminPanel.setHgap(5);
        adminPanel.add(treeContainer,0 ,0);
        adminPanel.add(vbox, 1,0);

        //Lambda function that attaches an event listener to tree items so that when a user presses on one of them an event triggers and runs this code
        treeView.getSelectionModel().selectedItemProperty().addListener((ChangeListener) (observable, oldValue, newValue) -> {
            //In my implementation, I disable the ability to add users or groups until you navigate through a treeview item so that you can add one to the root you are at
            TreeItem<UserInterface> selectedItem = (TreeItem<UserInterface>) newValue;
            TreeItem<UserInterface> previousItem = (TreeItem<UserInterface>) oldValue;
            //differentiate between users and user groups to get correct info displayed in textfields, and opens up certain features to be accessible by the user
            if(selectedItem.getValue() instanceof User){
                adminPanel.getUserId().setText(String.valueOf(selectedItem.getValue()));
                adminPanel.getGroupId().setText(String.valueOf(selectedItem.getParent().getValue()));
                adminPanel.getOpenUserView().setDisable(false);
                adminPanel.getAddUser().setDisable(true);
                adminPanel.getAddGroup().setDisable(true);
                //HW 3
                adminPanel.getGroupCreationTime().setText("Group Created: " + ((UserGroup)selectedItem.getParent().getValue()).getGroupCreated());
            }
            else if(selectedItem.getValue() instanceof UserGroup){
                adminPanel.getUserId().setText("");
                adminPanel.getGroupId().setText(String.valueOf(selectedItem.getValue()));
                adminPanel.getOpenUserView().setDisable(true);
                adminPanel.getAddUser().setDisable(false);
                adminPanel.getAddGroup().setDisable(false);
                //HW 3
                adminPanel.getGroupCreationTime().setText("Group Created: "+((UserGroup)selectedItem.getValue()).getGroupCreated().toString());
            }

            adminPanel.getAddUser().setOnAction(event -> {
                TextInputDialog input = new TextInputDialog();
                input.setTitle("User addition");
                input.setHeaderText("Adding a new User");
                input.getDialogPane().setContentText("User Name:");
                Optional<String> result = input.showAndWait();
                TextField inputTF = input.getEditor();
                //Soft input validation, also does not allow adding anything under users only groups, if still attempted nothing will change and program will not produce an error/crash
                //Also can't add a new user if the given ID was already given to another created user at an earlier point
                if(inputTF.getText()!=null && inputTF.getText().length()!=0 && newValue!=null && ((TreeItem<UserInterface>) newValue).getValue() instanceof UserGroup){
                    User newUser = new User(inputTF.getText());
                    long userCreated = System.currentTimeMillis();
                    users.add(newUser);
                    userIds.add(newUser.toString());
                    map.put(newUser, newUser.getUserCreated());
                    adminPanel.createBranch(newUser, (TreeItem<UserInterface>)newValue);
                }
            });
            adminPanel.getAddGroup().setOnAction(event -> {
                TextInputDialog input = new TextInputDialog();
                input.setTitle("Group Addition");
                input.setHeaderText("Adding a new Group");
                input.getDialogPane().setContentText("Group Name:");
                Optional<String> result = input.showAndWait();
                TextField inputTF = input.getEditor();
                //Soft input validation, also does not allow adding anything under users only groups, if still attempted nothing will change and program will not produce an error/crash
                if(inputTF.getText()!=null && inputTF.getText().length()!=0 && newValue!=null && ((TreeItem<UserInterface>) newValue).getValue() instanceof UserGroup){
                    UserGroup newUserGroup = new UserGroup(inputTF.getText());
                    adminPanel.createBranch(newUserGroup, (TreeItem<UserInterface>)newValue, new Circle(5, Color.BLACK));
                    groups.add(newUserGroup);
                    groupIds.add(newUserGroup.toString());
                }
            });
            adminPanel.getOpenUserView().setOnAction(event -> {
                Stage stage = new Stage();
                stage.setTitle("User View");
                GridPane gridPane = new GridPane();
                ListView<String> userListView = new ListView<>();
                userListView.setPrefSize(50,125);
                userListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
                userListView.getItems().addAll(((TreeItem<User>) newValue).getValue().getFollowerIds());
                Label creationTime = new Label("Time Created: " + ((User)selectedItem.getValue()).getCreationTime());
                Label updateTime = new Label("Last Time Updated: " + ((User)selectedItem.getValue()).getLastUpdated());
                Label followers = new Label("Followers");
                Button followUserButton = new Button("Follow " + selectedItem.getValue().toString());
                followUserButton.setDisable(true);
                //Enable follow user button if the previously selected Tree Item was a different user
                if(previousItem.getValue() instanceof User && selectedItem.getValue() instanceof User && previousItem != selectedItem){
                    followUserButton.setDisable(false);
                }
                followUserButton.setOnAction(event2 -> {
                    if(previousItem.getValue() instanceof User && selectedItem.getValue() instanceof User && previousItem!=selectedItem && !((User) previousItem.getValue()).getFollowingIds().contains(selectedItem.getValue().toString())){
                        ((User) previousItem.getValue()).followUser((User) selectedItem.getValue(),selectedItem.getValue().toString());
                        userListView.getItems().add(String.valueOf(((TreeItem<User>) oldValue).getValue()));
                    };
                });
                ListView<String> newsFeed = new ListView<>();
                newsFeed.setPrefSize(75,125);
                newsFeed.getItems().addAll(((User)selectedItem.getValue()).getTweetMsgs());
                TextField tweet = new TextField();
                Label news = new Label("Tweets: ");
                Button postTweet = new Button("Post Tweet");
                postTweet.setOnAction(event1 -> {
                    ((TreeItem<User>)newValue).getValue().Tweet(tweet.getText());
                    newsFeed.getItems().clear();
                    newsFeed.getItems().addAll(((User)selectedItem.getValue()).getTweetMsgs());
                    tweet.setText("");
                    updateTime.setText("Last Time Updated: " + ((User)selectedItem.getValue()).getLastUpdated());
                });

                Button refresh = new Button("Refresh");
                refresh.setOnAction(evnt -> {
                    newsFeed.getItems().clear();
                    newsFeed.getItems().addAll(((User)selectedItem.getValue()).getTweetMsgs());
                    //HW 3
                    updateTime.setText("Last Time Updated: " + ((User)selectedItem.getValue()).getLastUpdated());
                });

                gridPane.addRow(0, adminPanel.getUserIdLabel(), adminPanel.getUserId(), followUserButton);
                gridPane.addRow(1, creationTime);
                gridPane.addRow(2, followers, userListView);
                gridPane.addRow(3, postTweet, tweet);
                gridPane.addRow(4, news, newsFeed);
                gridPane.addRow(5,refresh, updateTime);
                gridPane.setVgap(20);
                gridPane.setHgap(20);

                Scene scene = new Scene(gridPane, 600, 600);             
                stage.setScene(scene);
                stage.show();


            });

        });
        adminPanel.getShowGroupTotal().setOnAction(event ->{
            SysEntryVisitor visitor = new ShowGroupTotalSysEntryVisitor();
            if(groups.size()>0) {
                groups.get(0).accept(visitor);
            }
        });

        adminPanel.getShowUserTotal().setOnAction(event -> {
            SysEntryVisitor visitor = new ShowUserTotalSysEntryVisitor();
            if(users.size()>0) {
                users.get(0).accept(visitor);
            }
        });
        adminPanel.getShowPosPercent().setOnAction(event -> {
            SysEntryVisitor visitor = new ShowPositivePercentSysEntryVisitor();
            if(users.size()>0) {
                users.get(0).accept(visitor);
            }
        });
        adminPanel.getShowMsgTotal().setOnAction(event -> {
            SysEntryVisitor visitor  = new ShowMessageTotalSysEntryVisitor();
            if(users.size()>0) {
                users.get(0).accept(visitor);
            }
        });
        //HW 3
        adminPanel.getIdVerification().setOnAction(event -> {
            if(hasDuplicates(userIds) || hasDuplicates(groupIds) || hasSpaces(userIds) || hasSpaces(groupIds)){
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText("User/Group ID Verification");
                alert.setContentText("Detected an invalid User/Group ID (Non-unique ID or a an ID containing a blank space)");
                alert.show();
            }
        });
        //HW 3
        adminPanel.getRecentUpdate().setOnAction(event -> {
            TreeMap<User, Long> treeMap = new TreeMap<>(map);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Recent Updates");
            alert.setContentText("User with most recent updates: " + treeMap.lastKey());
            alert.show();
        });

        Scene myScene = new Scene(adminPanel, 650, 400);
        primaryStage.setScene(myScene);
        primaryStage.setTitle("Twitter");
        primaryStage.show();
    }
    public static <T> boolean hasSpaces(List<T> list){
        for(T element : list){
            if(element.toString().contains(" ")){
                return true;
            }
        }
        return false;
    }

    public static <T> boolean hasDuplicates(List<T> list){
        ArrayList<T> newList = new ArrayList<T>();
        for (T element : list) {
            if (!newList.contains(element)) {
                newList.add(element);
            }
            else if(newList.contains(element)){
                return true;
            }
        }
        return false;
    }
}
