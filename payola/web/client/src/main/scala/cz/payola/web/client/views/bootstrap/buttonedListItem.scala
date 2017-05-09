package cz.payola.web.client.views.bootstrap

import cz.payola.web.client.views.elements.lists.ListItem
import cz.payola.web.client.View
import cz.payola.web.client.events.SimpleUnitEvent
import cz.payola.web.client.views.elements._
import cz.payola.web.client.views.ComposedView
import scala.collection.mutable.ListBuffer

class ButtonedListItem(icon: String, content: Seq[View], buttonEnabled: Boolean = true, cssClass: String = "") extends ComposedView {

    val buttonEvent = new SimpleUnitEvent[this.type]

    private val button = new Anchor(List(new Icon(icon)), "#", "pull-right").setAttribute("style", "background-color: #f5f5f5; border-radius: 4px;")
    button.hide()
    button.mouseClicked += { e =>
        buttonEvent.triggerDirectly(this)
        false
    }

    val extendedContent = new ListBuffer[View]()
    extendedContent ++= content
    if(buttonEnabled) {
        extendedContent += button
    }

    val listItem = new ListItem(extendedContent.toList, cssClass).setAttribute("style", "display: flex; white-space: nowrap;")
    if (buttonEnabled) {
        listItem.mouseOut += { e =>
            button.hide()
            false
        }
        listItem.mouseMoved += { e =>
            button.show("block; background-color: #f5f5f5; border-radius: 4px;")
            false
        }
    }

    def getAttribute(name: String) = listItem.getAttribute(name)

    def setAttribute(name: String, value: String): this.type = {
        listItem.setAttribute(name, value)
        this
    }

    def createSubViews = List(listItem)

    def removeCssClass(cssClass: String): this.type = {
        listItem.removeCssClass(cssClass)
        this
    }

    def addCssClass(cssClass: String): this.type = {
        listItem.addCssClass(cssClass)
        this
    }
}
