package pim.views.gui.main;

import pim.generic.Iface;
import pim.model.orm.PIMEntity;
import pim.views.gui.generic.Colors;
import pim.views.gui.generic.ElemStatus;
import pim.views.gui.generic.Fonts;

import javax.swing.*;

class CalItemPanel extends JPanel {
    private Iface iface;

    private JLabel dayLabel;
    private ElemStatus status;
    PIMEntity[] entities;
    private JLabel entitiesNumLabel;

    CalItemPanel(Iface iface) {
        super();
        this.iface = iface;

        this.setLayout(null);
        this.setBackground(Colors.CAL_ITEM_DEFAULT_BG);


        this.dayLabel = new JLabel("", JLabel.LEFT);
        this.dayLabel.setBounds(
                CalItemPanel.dayLabelLeft, CalItemPanel.dayLabelTop,
                CalItemPanel.dayLabelWidth, CalItemPanel.dayLabelHeight
        );
        this.dayLabel.setFont(Fonts.CAL_ITEM_DAY_DEFAULT);
        this.dayLabel.setForeground(Colors.CAL_ITEM_DEFAULT_F);

        this.add(this.dayLabel);

        this.entitiesNumLabel = new JLabel("0");
        this.entitiesNumLabel.setFont(Fonts.CAL_ITEM_DAY_ACTIVE);
        this.entitiesNumLabel.setForeground(Colors.CAL_TITLE_MONTH_F);
        this.entitiesNumLabel.setBounds(5,25, 10, 10);
        this.entitiesNumLabel.setVisible(false);
        this.add(this.entitiesNumLabel);

        this.status = ElemStatus.DEFAULT;
    }

    public void setDay(int day) {
        this.dayLabel.setText(Integer.toString(day));
        this.entitiesNumLabel.setVisible(false);
    }
    public int getDay() {
        return Integer.parseInt(this.dayLabel.getText());
    }

    public void setStatus(ElemStatus status) {
        if (status == this.status) {
            return;
        }

        switch (status) {
            case HOVER:
                this.setBackground(Colors.CAL_ITEM_HOVER_BG);
                this.dayLabel.setFont(Fonts.CAL_ITEM_DAY_DEFAULT);
                this.dayLabel.setForeground(Colors.CAL_ITEM_DEFAULT_F);
                this.status = ElemStatus.HOVER;
                break;
            case ACTIVE:
                this.setBackground(Colors.CAL_ITEM_HOVER_BG);
                this.dayLabel.setFont(Fonts.CAL_ITEM_DAY_ACTIVE);
                this.dayLabel.setForeground(Colors.CAL_ITEM_DEFAULT_F);
                this.status = ElemStatus.ACTIVE;
                break;
            case UNABLE:
                this.setBackground(Colors.CAL_ITEM_UNABLE_BG);
                this.dayLabel.setFont(Fonts.CAL_ITEM_DAY_DEFAULT);
                this.dayLabel.setForeground(Colors.CAL_ITEM_UNABLE_F);
                this.status = ElemStatus.UNABLE;
                break;
            case SELECTED:
                this.setBackground(Colors.CAL_ITEM_SELECTED_BG);
                this.dayLabel.setFont(Fonts.CAL_ITEM_DAY_ACTIVE);
                this.dayLabel.setForeground(Colors.CAL_ITEM_DEFAULT_F);
                this.status = ElemStatus.SELECTED;
                break;
            case TODAY:
                this.setBackground(Colors.CAL_ITEM_TODAY_BG);
                this.dayLabel.setFont(Fonts.CAL_ITEM_DAY_DEFAULT);
                this.dayLabel.setForeground(Colors.CAL_ITEM_DEFAULT_F);
                this.status = ElemStatus.TODAY;
                break;
            case DEFAULT:
            default:
                this.setBackground(Colors.CAL_ITEM_DEFAULT_BG);
                this.dayLabel.setFont(Fonts.CAL_ITEM_DAY_DEFAULT);
                this.dayLabel.setForeground(Colors.CAL_ITEM_DEFAULT_F);
                this.status = ElemStatus.DEFAULT;
                break;
        }
    }
    public ElemStatus getStatus() {
        return this.status;
    }

    public void setEntities(PIMEntity[] entities) {
        this.entities = entities;
        if (entities != null && entities.length > 0) {
            this.entitiesNumLabel.setText(Integer.toString(entities.length));
            this.entitiesNumLabel.setVisible(true);
        }
        else {
            this.entitiesNumLabel.setVisible(false);
        }
    }
    private static final int dayLabelLeft = 5;
    private static final int dayLabelTop = 5;
    private static final int dayLabelWidth = 50;
    private static final int dayLabelHeight = 16;
}
