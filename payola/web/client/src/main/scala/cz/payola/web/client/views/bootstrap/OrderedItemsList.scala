package cz.payola.web.client.views.bootstrap

import cz.payola.web.client.views.ComposedView
import cz.payola.web.client.View
import cz.payola.web.client.views.elements.lists._
import cz.payola.web.client.views.elements.form.fields.TextInput
import cz.payola.web.client.views.elements._
import cz.payola.web.client.views.elements.form.Field
import collection.mutable
import collection.mutable.ListBuffer
import scala.Some
import s2js.adapters.html
import cz.payola.common.entities.settings.LabelItem
import cz.payola.web.client.presenters.entity.settings.ClassCustomizationEventArgs
import s2js.compiler.javascript

class OrderedItemsList(name: String, listElements: Seq[LabelItem] = Nil, cssClass: String = "")
    extends ComposedView with Field[Option[String]] {

    private var parentHtmlElement: Option[html.Element] = None

    private var orderNum = -1 //should be defined inside the following but s2js does not process it correctly

    var allListItems: mutable.Buffer[OrderedListItem] =
        ListBuffer[OrderedListItem]() ++ {
            if(!listElements.exists(_.userDefined)) { //conditionally add editable field
                orderNum += 1
                val item = new OrderedListItem(new LabelItem("", true, false), orderNum)
                item.textArea.changed += { e =>
                    this.changed.triggerDirectly(this)
                    false
                }
                List(item)
            } else {
                List[OrderedListItem]()
            }
        } ++ listElements.map{element =>
            orderNum += 1
            new OrderedListItem(element, orderNum)
        }

    allListItems.foreach{ listItem =>
        listItem.buttonAccepted.mouseClicked += { e =>
            changed.triggerDirectly(this)
            false
        }
        listItem.buttonForbidden.mouseClicked += { e =>
            changed.triggerDirectly(this)
            false
        }
        listItem.textArea.changed += { e =>
            if(listItem.labelItem.userDefined)
                changed.triggerDirectly(this)
            false
        }
    }

    val itemsList = new UnorderedList(allListItems, cssClass+" nav nav-list ui-sortable").setAttribute("id", "sortableListItems")

    val listContainer = new Div(List(itemsList), "modal-inner-view well no-padding")

    def createSubViews: Seq[View] = {
        List(listContainer)
    }

    def formHtmlElement = allListItems(0).formHtmlElement

    def value = {
        var result = ""
        allListItems.foreach{ item =>
            if(item.value.isDefined) {
                if(item.accepted) { result += "T" } else { result += "F" }
                result += { if(item.labelItem.userDefined) { "U" } else { "" } } + "-" + item.value.get + ";"
            } else {
                result += ";"
            }
        }
        Some(result)
    }

    def isActive = allListItems(0).isActive

    def isActive_=(value: Boolean) {
        allListItems(0).isActive = value
    }

    protected def updateValue(newValue: Option[String]) {
        if (newValue.isDefined) {
            val values: Array[String] = newValue.get.split(";")

            for(pointer <- 0 to values.length) {
                allListItems(pointer).updateValue(Some(values(pointer)))
            }
        }
    }

    private def update() {
        if(parentHtmlElement.isDefined) {
            this.destroy()
            this.render(parentHtmlElement.get)
        }
    }

    override def destroy() {
        super.destroy()
        disableSortable();
    }

    override def render(parent: html.Element) {
        super.render(parent)
        parentHtmlElement = Some(parent)
        initSortable()
    }

    @javascript (
        """
          $( "#sortableListItems" ).sortable({
                axis: 'y',
                items: '.sortedListItem',
                start: function() {
                    $(this).find("li:not(.sortedListItem)").each(function() {
                        $(this).data("fixedIndex", $(this).index());
                    })
                },
                change: function() {
                    $(this).find("li:not(.sortedListItem)").each(function() {
                        if($(this).data("fixedIndex") != 0) {
                            $(this).detach().insertAfter(
                                $("#sortableListItems li:eq(" + ($(this).data("fixedIndex")-1) + ")"));
                        }
                    });
                }
          });
          $( "#sortableListItems" ).disableSelection();
          $( "#sortableListItems" ).on( "sortupdate", function( event, ui ) {
                var itemsOrder = scala.collection.mutable.ListBuffer.get().$apply();
                var listElements = document.getElementById("sortableListItems").children;
                for(var i = 0; i < listElements.length; ++i) { //skip first non soratble element
                    itemsOrder.$plus$eq(listElements[i].attributes["orderNumber"].value)
                }
                self.updateItemListOrder(itemsOrder.toList());
               } );
        """)
    private def initSortable(){}

    @javascript (
        """
          $( "#sortableListItems" ).sortable('cancel');
        """)
    private def disableSortable(){}

    private def updateItemListOrder(order: List[Int]) {

        val reorderedList = new ListBuffer[OrderedListItem]()

        //sort the items by the new order
        order.foreach{ orderNumber =>
            allListItems.filter(_.orderNumber == orderNumber).foreach(reorderedList += _)
        }

        //number the newly sorted items
        var positionNumber = 0
        reorderedList.foreach{ item =>
            item.orderNumber = positionNumber
            positionNumber += 1
        }

        allListItems = reorderedList

        changed.triggerDirectly(this)
    }
}



class OrderedListItem(val labelItem: LabelItem, val numberInOrder: Int) extends ComposedView with Field[Option[String]] {

    val grabberIcon = new Icon(Icon.resize_vertical).setAttribute("style", "padding: 10px;")
    val _buttonAccepted = new Anchor(List(new Icon(Icon.ok).setAttribute("style", "padding: 10px;")))
    val _buttonForbidden = new Anchor(List(new Icon(Icon.remove).setAttribute("style", "padding: 10px;")))
    var _orderNumber = numberInOrder

    def orderNumber_=(newValue: Int) {
        _orderNumber = newValue
        listItem.setAttribute("orderNumber", newValue.toString)
    }

    def orderNumber = _orderNumber

    var accepted = labelItem.accepted

    if(labelItem.accepted) {
        _buttonForbidden.hide()
        _buttonAccepted.show("inline")
    } else {
        _buttonAccepted.hide()
        _buttonForbidden.show("inline")
    }

    val _textArea = new TextInput(if(labelItem.userDefined) { "U" } else { "" }, labelItem.value).setAttribute("style", "width: 200px;")
    if(!labelItem.userDefined) {
        textArea.disable()
    }

    val listItem = new ListItem(List(
        new Div(List(grabberIcon, _textArea, _buttonAccepted, _buttonForbidden)).setAttribute("style", "display: inline-flex; padding-bottom: 5px;")
    ), "sortedListItem sortableItem").setAttribute("orderNumber", orderNumber.toString)

    _buttonAccepted.mouseClicked += { e =>
        buttonAccepted.hide()
        buttonForbidden.show("inline")

        accepted = false
        false
    }
    _buttonForbidden.mouseClicked += { e =>
        buttonForbidden.hide()
        buttonAccepted.show("inline")

        accepted = true
        false
    }

    def textArea = _textArea

    def buttonAccepted = _buttonAccepted

    def buttonForbidden = _buttonForbidden

    def createSubViews(): Seq[View] = {
        List(listItem)
    }

    def formHtmlElement = textArea.htmlElement

    def value: Option[String] = {
        Some(textArea.value)
    }

    def updateValue(newValue: Option[String]) {
        textArea.updateValue(newValue.map(_.toString).getOrElse(""))
    }

    def isActive = textArea.isActive

    def isActive_=(newValue: Boolean) {
        textArea.isActive = newValue
    }
}