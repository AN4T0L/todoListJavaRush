package com.anatol.todo;

import com.vaadin.annotations.Theme;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.Position;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.*;
import com.vaadin.ui.renderers.ProgressBarRenderer;
import org.springframework.beans.factory.annotation.Autowired;


import java.util.*;

import static com.vaadin.ui.Notification.TYPE_WARNING_MESSAGE;

@SpringUI
@Theme("valo")
public class TodoUI extends UI {

    @Autowired
    private TdService tdService;
    private Grid grid = new Grid();
    private BeanItemContainer container = null;
    private Todo td;
    //map для формирования страниц
    private HashMap<Integer, List<Todo>> map= null;
    //переключатель для сортировки по 3 ипостасям
    private Integer switcher = 0;
    //номер страницы
    private static int pageNumber = 0;
//    private int maxPageNumber = (int)Math.round(tdService.getAllTodo().size()/10.0 + 0.5d)-1;
    //Объявление объектов вадин
    private Button next = new Button("Ne}{t");
    private Button prev = new Button("Previ0u$");
    private Button save = new Button("$@ve");
    private Button add = new Button("Add new");
    private Button sortComplete = new Button("Sort");
    private Button delete = new Button("Rem0ve");
    private TextField tfDescr = new TextField("Description");
    private TextField tfProgr = new TextField("Progress");
    private TextField tfId  = new TextField("id");
    private VerticalLayout gridLay = new VerticalLayout(grid);
    private HorizontalLayout nextprev = new HorizontalLayout(prev,next);
    private HorizontalLayout general = new HorizontalLayout();
    private HorizontalLayout buttons = new HorizontalLayout(add,save,delete);
    private HorizontalLayout sort = new HorizontalLayout(sortComplete);
    private VerticalLayout field = new VerticalLayout(tfDescr, tfProgr);



    @Override
    protected void init(VaadinRequest vaadinRequest) {
        updateGrid(pageNumber);
        grid.setColumns("id","description","progress");
        Locale ru = new Locale("ru");
        Locale.setDefault(ru);
        tfDescr.setLocale(ru);
        grid.setLocale(ru);
        grid.getColumn("progress").setRenderer(new ProgressBarRenderer());

        //listeners событий
        grid.addSelectionListener(e -> updateForm());
        next.addClickListener(e -> nextPage());
        prev.addClickListener(e -> prevPage());
        add.addClickListener(e -> addNewTask());
        delete.addClickListener(e -> removeTask());
        save.addClickListener(e -> editTask());
        sortComplete.addClickListener(e -> setSortSwitcher());

        //формирование вида слоёв
        general.addComponent(gridLay);
        gridLay.addComponent(nextprev);
        general.addComponent(field);
        field.addComponent(buttons);
        field.addComponent(sort);
        setSpaces(true);
        nextprev.setMargin(true);
        gridLay.setMargin(true);
        field.setMargin(true);
        setContent(general);
    }

    //Переключатель сортировки
    private void setSortSwitcher() {
        if(switcher==0){
            switcher = 1;
            Notification.show("Complete");
        }
        else if(switcher==1){
            switcher = -1;
            Notification.show("In progress");}
        else if(switcher==-1){
            switcher= 0;
            Notification.show("All list");}

        updateGrid(pageNumber);


    }


    private void nextPage(){
      if(pageNumber<map.size()-1)updateGrid(++pageNumber);
    }
    private void prevPage(){
        if(pageNumber>0)updateGrid(--pageNumber);
    }

    //Выбор списка и добавление в контейнер
    private void updateGrid(int page){
        List<Todo> tdList = null;
        if(switcher==0)tdList = getCurrentList(page);
        else if(switcher==1)tdList = getCompleteList(page);
        else if(switcher==-1)tdList = getInProgressList(page);
        container = new BeanItemContainer<>(Todo.class,
                tdList);
        grid.setContainerDataSource(container);

    }

    //Формирует новое задание и добавляет в бд через сервис
    private void addNewTask() {
        try {
            if (tfDescr.isEmpty()) throw new Exception();
            if (isNumeric(tfDescr.getValue())) {
                Notification.show("WARNING", "Insert Characters in description form, please ",
                        TYPE_WARNING_MESSAGE);
            } else if (!isNumeric(tfProgr.getValue())) {
                Notification.show("WARNING", "Insert double value(delim:point) in progress form, please ",
                        TYPE_WARNING_MESSAGE);
            }
            else if(Double.parseDouble(tfProgr.getValue())>100 || Double.parseDouble(tfProgr.getValue())<1) throw new UnknownNumberFormatException();
            else {
                String descript = tfDescr.getValue();
                Double progress = Double.parseDouble(tfProgr.getValue());
                td = new Todo(descript, progress/100);
                tdService.insertTodo(td);
                Notification n = new Notification("INSERTED", "Description: " + descript
                        + "\nProgress: " + progress, Notification.Type.TRAY_NOTIFICATION);
                n.setDelayMsec(3000);
                n.setPosition(Position.TOP_CENTER);
                n.show(Page.getCurrent());
            }
        }catch (UnknownNumberFormatException e){
            Notification.show("WARNING", "Insert values from 1 to 100",
                    TYPE_WARNING_MESSAGE);
        }
        catch (Exception e) {
            Notification.show("WARNING", "Insert values in forms",
                    TYPE_WARNING_MESSAGE);
        }
        updateGrid(pageNumber);
    }

    //Редактируте задание
    private void editTask() {
        try {
            String newDescription = tfDescr.getValue();
            Double newProgress = Double.parseDouble(tfProgr.getValue())/100;
            String oldDescription = td.getDescription();
            Double oldProgress = td.getProgress();

            if(tfDescr.isEmpty());
            else{td.setDescription(newDescription);
            }
            if(tfProgr.isEmpty() );
            else{td.setProgress(newProgress);}
            tdService.updateTodo(td);
            String s;
            if(td.getDescription().equals(oldDescription)) s = "Description identical";
            else{
             s = "Description : " + oldDescription + " -> " + newDescription
                    ;}
            Notification n = new Notification("UPDATED", s  + "\nProgress: " +oldProgress * 100 + " -> " + newProgress * 100, Notification.Type.TRAY_NOTIFICATION);
            n.setDelayMsec(3000);
            n.setPosition(Position.TOP_CENTER);
            n.show(Page.getCurrent());
        }catch (Exception e){
            Notification.show("WARNING","Type progress in integer format",
                    TYPE_WARNING_MESSAGE);
        }
        updateGrid(pageNumber);
    }

    //Удаляет задание
    private void removeTask() {
            Integer id = td.getId();
            tdService.removeTodoById(id);
            Notification n = new Notification("REMOVED", "Description: " + td.getDescription()
                    + "\nProgress: " + td.getProgress()*100, Notification.Type.TRAY_NOTIFICATION);
            n.setDelayMsec(5000);
            n.setPosition(Position.TOP_CENTER);
            n.show(Page.getCurrent());

        updateGrid(pageNumber);
    }

    //Получение текущей страницы
    private List<Todo> getCurrentList(int page){
        map = new HashMap<>();
        List<Todo> list = tdService.getAllTodo();
        return getTodosLists(page, list);
    }

    //Формировние списка для страницы
    private List<Todo> getTodosLists(int page, List<Todo> list) {
        int countGrid = (int)Math.round(list.size()/10.0 + 0.5d);
        for (int j = 0; j < countGrid; j++) {
            ArrayList<Todo> tmp = new ArrayList<>();
            for (int k = j*10; k < (1+j)*10 && k<list.size(); k++) {
                tmp.add(list.get(k));
            }
            map.put(j,tmp);
        }
        return map.get(page);
    }

    //Получение выполненных дел
    private List<Todo> getCompleteList(int page){
        map = new HashMap<>();
        List<Todo> listCompleteTodo = tdService.getAllTodo();
        List<Todo> list = new ArrayList<>();
        for (int i = 0; i < listCompleteTodo.size(); i++) {
           if(Double.compare(listCompleteTodo.get(i).getProgress(),1.0)==0) list.add(listCompleteTodo.get(i));
        }
        return getTodosLists(page,list);
    }

    //Получение дел в процессе выполнения
    private List<Todo> getInProgressList(int page){
        map = new HashMap<>();
        List<Todo> listCompleteTodo = tdService.getAllTodo();
        List<Todo> list = new ArrayList<>();
        for (int i = 0; i < listCompleteTodo.size(); i++) {
            if(Double.compare(listCompleteTodo.get(i).getProgress(),1.0)!=0) list.add(listCompleteTodo.get(i));
        }
        return getTodosLists(page,list);
    }

    //Проверка на число
    private boolean isNumeric(String str)
    {
        try
        {
            double d = Double.parseDouble(str);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }


    private void setSpaces(boolean b){
        general.setSpacing(b);
        gridLay.setSpacing(b);
        field.setSpacing(b);
        buttons.setSpacing(b);
        nextprev.setSpacing(b);
    }
    //Заполнение форм на странице браузера при выделении строки в таблице
    private void updateForm() {
        if (grid.getSelectedRows().isEmpty()) {
        } else {
            td = (Todo) grid.getSelectedRow();
            tfDescr.setValue(td.getDescription());
            Double d = td.getProgress()*100;
            tfProgr.setValue(d.toString());

        }
    }





}
