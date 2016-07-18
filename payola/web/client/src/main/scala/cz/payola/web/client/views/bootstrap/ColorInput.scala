package cz.payola.web.client.views.bootstrap

import s2js.compiler.javascript
import cz.payola.common.visual.Color
import cz.payola.web.client.views.elements._
import cz.payola.web.client.views.elements.form._
import cz.payola.web.client.views.elements.form.fields._
import cz.payola.web.client.views.ComposedView

class ColorInput(name: String, initialValue: Option[Color], cssClass: String = "")
    extends ComposedView with Field[Option[Color]]
{
    private val colorInput = new TextInput(name, colorToText(initialValue)).setAttribute("style", "width:200px;")

    private val colorPreview = new Italic().setAttribute("style", "margin-left: 200px; width: 34px; height: 34px; border-radius: 0px 4px 4px 0px;")

    val clearButton = new Button(new Icon(Icon.remove)).setAttribute("style", "margin-top: -57px; margin-left: 238px;")

    initializeColorInput()
    value = initialValue
    updateColorPreview()

    clearButton.mouseClicked += { _ =>
        value = None
        false
    }
    colorInput.changed += { _ =>
        changed.triggerDirectly(this)
    }
    changed += { _ =>
        updateColorPreview()
    }

    def formHtmlElement = colorInput.htmlElement


    def createSubViews = List(new Div(List(
        new Div(List(
            colorInput,
            new Span(List(colorPreview), "add-on"),
            clearButton),
            "input-append color"
        )),"input-group")
    )

    def value: Option[Color] = {
        Color(colorInput.value)
    }

    def updateValue(newValue: Option[Color]) {
        colorInput.value = colorToText(newValue)
        colorInput.setAttribute("data-color", colorToRgb(newValue))
    }

    def isActive = colorInput.isActive

    def isActive_=(newValue: Boolean) {
        colorInput.isActive = newValue
    }

    private def updateColorPreview() {
        colorPreview.setAttribute("style", "margin-left: 200px;width: 34px;height: 34px;border-radius: 0px 4px 4px 0px; background-color: " + colorToRgb(value))
    }

    private def colorToRgb(color: Option[Color]): String = {
        color.map(_.toString).getOrElse("rgb(0, 0, 0)")
    }

    private def colorToText(color: Option[Color]): String = {
        color.map(_.toString).getOrElse("No color selected")
    }

    @javascript(
        """
            var colorPicker = jQuery(self.colorInput.htmlElement).colorpicker({ format: 'rgb' }).css("z-index", "1000");
            colorPicker.on('show', function(e) {
                colorPicker.isShown = true;
            });
            colorPicker.on('hide', function(e) {
                if (colorPicker.isShown) {
                    var color = e.color.toRGB();
                    self.value_$eq(new scala.Some(new cz.payola.common.visual.Color(color.r, color.g, color.b)));
                    colorPicker.isShown = false;
                }
            });
        """)
    private def initializeColorInput() {}
}
