package pim.views.gui.main;

import pim.controller.Cal;
import pim.generic.Iface;
import pim.model.Response;
import pim.model.orm.PIMAppointment;
import pim.model.orm.PIMEntity;
import pim.model.orm.PIMTodo;
import pim.views.gui.generic.Colors;
import pim.views.gui.generic.ElemStatus;
import pim.views.gui.sidebar.EntitiesListPanel;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Calendar;

public class CalPanel extends JPanel {

    private Iface iface;

    private CalItemPanel[][] calItemPanel;
    private Cal cal;
    private MainFrame mainFrame;

    CalPanel(Iface iface, MainFrame mainFrame) {
        super();
        this.iface = iface;
        this.mainFrame = mainFrame;

        this.calItemPanel = new CalItemPanel[6][7];
        this.initGUI();

        this.autoResize();
    }

    private void initGUI() {
        this.setLayout(null);

        this.setBackground(Colors.CAL_ITEM_BORDER);

        MineMouseListener mouseListener = new MineMouseListener(this);
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                this.calItemPanel[row][col] = new CalItemPanel(this.iface);
                this.calItemPanel[row][col].addMouseListener(mouseListener);

                this.add(calItemPanel[row][col]);
            }
        }
    }

    void autoResize() {
        int width = this.getWidth();
        int height = this.getHeight();

        int itemWidth = (width - 6) / 7;
        int itemHeight = (height - 6) / 6;
        int remainderWidth = width - 6 - itemWidth * 7;
        int remainderHeight = height - 6 - itemHeight * 6;
        int rowTop = 1;
        for (int row = 0; row < 6; row++) {
            int rowHeight = row < remainderHeight ? itemHeight + 1 : itemHeight;
            int colLeft = 0;
            for (int col = 0; col < 7; col++) {

                int colWidth = col < remainderWidth ? itemWidth + 1 : itemWidth;

                this.calItemPanel[row][col].setBounds(colLeft, rowTop, colWidth, rowHeight);

                colLeft += colWidth + 1;
            }
            rowTop += rowHeight + 1;
        }
    }

    public void setDate(Cal cal) {
        this.cal = cal;

        Cal.CalItem[][] calItems = this.cal.getItems();

        Calendar calendar = Calendar.getInstance();

        PIMEntity[] entities = null;
        if (this.iface.getUser() != null) {
            Calendar calTmp1 = Calendar.getInstance();
            calTmp1.set(cal.getDate().get(Calendar.YEAR), cal.getDate().get(Calendar.MONTH), 1);
            Calendar calTmp2 = Calendar.getInstance();
            calTmp2.set(cal.getDate().get(Calendar.YEAR), cal.getDate().get(Calendar.MONTH), 1);
            calTmp2.add(Calendar.DAY_OF_YEAR, 30);
            Response res = this.iface.getDataAPI().getEntities(calTmp1.getTime(), calTmp2.getTime(), this.iface.getUser());
            if (res.statusCode == 200) {
                entities = res.data;
            }
            else {
                throw new RuntimeException("DataAPI 返回非预期状态");
            }
        }

        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {

                this.calItemPanel[row][col].setDay(calItems[row][col].day);
                this.calItemPanel[row][col].setStatus(calItems[row][col].able ? ElemStatus.DEFAULT : ElemStatus.UNABLE);

                if (
                        this.cal.getDate().get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                        this.cal.getDate().get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                        calItems[row][col].day == calendar.get(Calendar.DAY_OF_MONTH) &&
                        calItems[row][col].able
                ) {
                    this.calItemPanel[row][col].setStatus(ElemStatus.TODAY);
                }

                if (this.iface.getUser() != null && calItems[row][col].able) {
                    ArrayList<PIMEntity> entitiesList = new ArrayList<PIMEntity>();
                    for (PIMEntity entity: entities) {
                        if (entity instanceof PIMTodo && ((PIMTodo) entity).getDate().getDate() == calItems[row][col].day) {
                            entitiesList.add(entity);
                        }
                        else if (entity instanceof PIMAppointment && ((PIMAppointment) entity).getDate().getDate() == calItems[row][col].day) {
                            entitiesList.add(entity);
                        }
                    }
                    this.calItemPanel[row][col].setEntities(entitiesList.toArray(new PIMEntity[0]));
                }
            }
        }



    }


    private static class MineMouseListener implements MouseListener {

        CalItemPanel todayPanel;
        private CalPanel panel;
        MineMouseListener(CalPanel panel) {
            this.panel = panel;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getSource() instanceof CalItemPanel && this.panel.iface.getUser() != null) {
                if (((CalItemPanel) e.getSource()).entities != null && ((CalItemPanel) e.getSource()).entities.length > 0)
                this.panel.mainFrame.sideBarPanel.panelSwitch(new EntitiesListPanel(
                        this.panel.mainFrame.sideBarPanel,
                        this.panel.mainFrame.sideBarPanel.mainPanel,
                        ((CalItemPanel) e.getSource()).entities
                ));
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.getSource() instanceof CalItemPanel) {
                CalItemPanel eventSource = ((CalItemPanel) e.getSource());
                if (eventSource.getStatus() != ElemStatus.UNABLE) {
                    eventSource.setStatus(ElemStatus.ACTIVE);
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.getSource() instanceof CalItemPanel) {
                CalItemPanel eventSource = ((CalItemPanel) e.getSource());
                if (eventSource.getStatus() == ElemStatus.ACTIVE && eventSource.getStatus() != ElemStatus.UNABLE) {
                    eventSource.setStatus(ElemStatus.HOVER);
                }
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            if (e.getSource() instanceof CalItemPanel) {
                CalItemPanel eventSource = ((CalItemPanel) e.getSource());
                if (eventSource.getStatus() == ElemStatus.TODAY) {
                    this.todayPanel = eventSource;
                }
                if (eventSource.getStatus() != ElemStatus.UNABLE) {
                    eventSource.setStatus(ElemStatus.HOVER);
                }
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            if (e.getSource() instanceof CalItemPanel) {
                CalItemPanel eventSource = ((CalItemPanel) e.getSource());
                if (eventSource.getStatus() != ElemStatus.UNABLE) {
                    eventSource.setStatus(eventSource == this.todayPanel ? ElemStatus.TODAY : ElemStatus.DEFAULT);
                }
            }
        }
    }
}
