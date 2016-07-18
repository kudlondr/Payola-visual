package cz.payola.web.client.views.bootstrap

import cz.payola.web.client.views.elements._
import cz.payola.web.client.views.elements.form._
import cz.payola.web.client.views.elements.form.fields._
import cz.payola.web.client.views.ComposedView
import cz.payola.web.client.views.bootstrap.element.AppendToUserCustButton
import cz.payola.web.client.events.SimpleUnitEvent
import s2js.adapters.browser._
import cz.payola.common.ValidationException
import cz.payola.web.client.models.PrefixApplier

class ConditionTextInput(availableValues: Seq[String], name: String, initialValue: String,
    prefixApplier: PrefixApplier, cssClass: String = "", style: String = "")
    extends ComposedView with Field[String]
{
    val delayedChanged = new SimpleUnitEvent[this.type]

    private var delayedChangedTimeout: Option[Int] = None

    private val infoText = new Text("")

    private val textInput = new TextInput(name, initialValue).setAttribute("style", style)

    private val setButton = new AppendToUserCustButton(availableValues, "Select",
        "Specify value of the property", "Property values", "Values available in current graph: ",
        "", setValue, prefixApplier, "Custom:")

    setButton.appendButton.mouseClicked += { e =>
        setButton.openPopup()
        false
    }
    setButton.appendButton.setAttribute("style", "margin-top: -57px; margin-left: 204px;")

    def setValue(value: String): Boolean = {
        textInput.value = value
        true
    }

    private val clearButton = new Button(new Icon(Icon.remove))

    clearButton.mouseClicked += { _ =>
        value = ""
        false
    }
    clearButton.setAttribute("style", "margin-top: -57px; margin-left: 0px;")

    textInput.changed += { _ =>
        delayedChangedTimeout.foreach(window.clearTimeout(_))
        delayedChangedTimeout = Some(window.setTimeout({ () => delayedChanged.triggerDirectly(this)}, 500))
        true
    }

    def setState(exception: ValidationException, fieldName: String) {
        if (fieldName == exception.fieldName) {
            setError(exception.message)
        } else {
            setOk()
        }
    }

    def setError(errorMessage: String) {
        infoText.text = errorMessage
        controlGroup.removeCssClass("success")
        controlGroup.addCssClass("error")
    }

    def setOk() {
        infoText.text = ""
        controlGroup.removeCssClass("error")
        controlGroup.addCssClass("success")
    }

    textInput.changed += { _ =>
        changed.triggerDirectly(this)
    }

    def formHtmlElement = textInput.htmlElement


    val controlGroup = new Div(List(
        new Div(List(
            textInput,
            setButton,
            clearButton,
            new Span(List(infoText), "help-inline")),
            "controls"
        )),
        "control-group "
    )

    def createSubViews = List(controlGroup)

    def value: String = {
        textInput.value
    }

    def updateValue(newValue: String) {
        textInput.value = newValue
    }

    def isActive = textInput.isActive

    def isActive_=(newValue: Boolean) {
        textInput.isActive = newValue
    }
}
