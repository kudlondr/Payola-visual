package cz.payola.web.client.views.graph.table

import s2js.adapters.browser._
import s2js.adapters.html
import cz.payola.common.rdf._
import cz.payola.web.client.View
import cz.payola.web.client.views.VertexEventArgs
import cz.payola.web.client.views.elements._
import cz.payola.web.client.views.bootstrap.Icon
import cz.payola.web.client.views.graph.PluginView
import cz.payola.web.client.models.PrefixApplier
import form.fields.TextInput
import cz.payola.web.client.views.bootstrap.modals.FatalErrorModal
import cz.payola.web.shared.transformators.TripleTableTransformator

abstract class TablePluginView(name: String, prefixApplier: Option[PrefixApplier])
    extends PluginView[Graph](name, prefixApplier)
{
    protected val tablePluginWrapper = new Div()
    protected val tableWrapper = new Div().setAttribute("style", "padding: 0 20px; min-height: 200px; margin:0 auto;")

    protected val allowedLinesOnPage = 50
    protected var currentPage = 0
    private var pagesCount = 0
    private var allRecordsCount = 0
    private var currentRecordsCount = 0

    def createSubViews = List(tablePluginWrapper)

    override def updateGraph(graph: Option[Graph], contractLiterals: Boolean = true) {
        updateGraphPage(graph, contractLiterals)
    }

    def updateGraphPage(graph: Option[Graph], contractLiterals: Boolean = true, page: Int = 0) {
        if (graph.isEmpty) {
            tablePluginWrapper.setAttribute("style", "height: 300px;")
            renderMessage(tablePluginWrapper.htmlElement, "The graph is empty...")
        } else {
            tablePluginWrapper.setAttribute("style", "")
            if (graph != currentGraph) {
                currentPage = page
                // Remove the old table.
                tableWrapper.removeAllChildNodes()
                tablePluginWrapper.removeAllChildNodes()

                tablePluginWrapper.htmlElement.appendChild(tableWrapper.htmlElement)

                renderTablePage(graph, 0)
                if(pagesCount != 0)
                    createListingTools().render(tablePluginWrapper.htmlElement)
            }
        }

        super.updateGraph(graph, true)
    }

    private def renderTablePage(graph: Option[Graph], pageNumber: Int) {
        tableWrapper.removeAllChildNodes()

        val table = document.createElement[html.Element]("table")
        table.className = "table table-striped table-bordered table-condensed"

        tableWrapper.htmlElement.appendChild(table)
        val counts = fillTable(graph, addElement(table, "thead"), addElement(table, "tbody"), pageNumber)
        currentRecordsCount = counts._1
        pagesCount = counts._2
        allRecordsCount = counts._3
    }

    protected def createListingTools(): View = {
        val infoText = new Text(getPageInfoText)

        val previousPageButton = new Button(new Text("Previous"), "", new Icon(Icon.step_backward))

        val firstPageButton = new Button(new Text("1"), "")

        val middlePageButton = new Button(new Text("2"), "")

        val currentPageValue = currentPage + 1
        val currentPageButton = new Button(new Text(currentPageValue.toString), "")

        val lastPageButton = new Button(new Text(pagesCount.toString), "")

        val jumpTextArea = new TextInput("jump", "")
        jumpTextArea.setAttribute("style", "width: 50px; display: inline")
        jumpTextArea.maxLength = 3
        val jumpButton = new Button(new Text("Go"), "", new Icon(Icon.play))
        jumpButton.setAttribute("style", "display: inline;")
        jumpTextArea.keyPressed += { e =>
            if(e.keyCode == 13) {
                jumpTextArea.hide()
                jumpButton.hide()
                val jumpToPageNumber = jumpTextArea.value.toInt - 1
                if(currentPage != jumpToPageNumber) {
                    val goingToPage = if(jumpToPageNumber >= 0 && jumpToPageNumber <= pagesCount) {
                        jumpToPageNumber } else { currentPage }

                    if(evaluationId.isDefined) {
                        paginateToPage(goingToPage)
                    } else {
                        currentPage = goingToPage
                        renderTablePage(currentGraph, currentPage)
                        infoText.text = getPageInfoText
                    }
                }
                false
            } else {
                true
            }
        }
        jumpButton.mouseClicked += { e =>
            jumpTextArea.hide()
            jumpButton.hide()
            val jumpToPageNumber = jumpTextArea.value.toInt - 1
            if(currentPage != jumpToPageNumber) {
                val goingToPage = if(jumpToPageNumber >= 0 && jumpToPageNumber <= pagesCount) {
                    jumpToPageNumber } else { currentPage }

                if(evaluationId.isDefined) {
                    paginateToPage(goingToPage)
                } else {
                    currentPage = goingToPage
                    renderTablePage(currentGraph, currentPage)
                    infoText.text = getPageInfoText
                }
            }
            false
        }

        val jumpDiv = new Div(List(jumpTextArea, jumpButton))
        jumpDiv.hide()

        val jumpToPageButton1 = new Button(new Text("..."), "")
        jumpToPageButton1.mouseClicked += { e =>
            if(jumpDiv.getAttribute("style").contains("display: none")) {
                jumpDiv.setAttribute("style", "display: inline; position: absolute; bottom: 40px; left: 140px;")
            } else {
                jumpDiv.hide()
            }
            false
        }

        val jumpToPageButton2 = new Button(new Text("..."), "")
        jumpToPageButton2.mouseClicked += { e =>
            if(jumpDiv.getAttribute("style").contains("display: none")) {
                jumpDiv.setAttribute("style", "display: inline; position: absolute; bottom: 40px; left: 140px;")
            } else {
                jumpDiv.hide()
            }
            false
        }

        val nextPageButton = new Button(new Text("Next"), "", new Icon(Icon.step_forward))
        nextPageButton.setIsEnabled(currentPage != pagesCount - 1)

        setEnabledPaginationButtons(previousPageButton, firstPageButton, middlePageButton, currentPageButton,
            lastPageButton, nextPageButton)


        previousPageButton.mouseClicked += { e =>
            if (currentPage != 0) {
                if(evaluationId.isDefined) {
                    paginateToPage(currentPage - 1)
                } else {
                    currentPage -= 1
                    renderTablePage(currentGraph, currentPage)
                    infoText.text = getPageInfoText
                    setEnabledPaginationButtons(previousPageButton, firstPageButton, middlePageButton,
                        currentPageButton, lastPageButton, nextPageButton)
                }
            }
            false
        }
        firstPageButton.mouseClicked += { e =>
            if (currentPage != 0) {
                if(evaluationId.isDefined) {
                    paginateToPage(0)
                } else {
                    currentPage = 0
                    renderTablePage(currentGraph, currentPage)
                    infoText.text = getPageInfoText
                    setEnabledPaginationButtons(previousPageButton, firstPageButton, middlePageButton,
                        currentPageButton, lastPageButton, nextPageButton)
                }
            }
            false
        }
        middlePageButton.mouseClicked += { e =>
            if (currentPage != 1) {
                if(evaluationId.isDefined) {
                    paginateToPage(1)
                } else {
                    currentPage = 1
                    renderTablePage(currentGraph, currentPage)
                    infoText.text = getPageInfoText
                    setEnabledPaginationButtons(previousPageButton, firstPageButton, middlePageButton,
                        currentPageButton, lastPageButton, nextPageButton)
                }
            }
            false
        }
        lastPageButton.mouseClicked += { e =>
            if (currentPage != pagesCount - 1) {
                if(evaluationId.isDefined) {
                    paginateToPage(pagesCount - 1)
                } else {
                    currentPage = pagesCount - 1
                    renderTablePage(currentGraph, currentPage)
                    infoText.text = getPageInfoText
                    setEnabledPaginationButtons(previousPageButton, firstPageButton, middlePageButton,
                        currentPageButton, lastPageButton, nextPageButton)
                }
            }
            false
        }
        nextPageButton.mouseClicked += { e =>
            if (currentPage != pagesCount - 1) {
                if(evaluationId.isDefined) {
                    paginateToPage(currentPage + 1)
                } else {
                    currentPage += 1
                    renderTablePage(currentGraph, currentPage)
                    infoText.text = getPageInfoText
                    setEnabledPaginationButtons(previousPageButton, firstPageButton, middlePageButton,
                        currentPageButton, lastPageButton, nextPageButton)
                }
            }
            false
        }

        val buttonsList = if(pagesCount <= 1) {
            List(previousPageButton, firstPageButton, nextPageButton, infoText)
        } else if(pagesCount == 2) {
            List(previousPageButton, firstPageButton, lastPageButton, nextPageButton, infoText)
        } else if(pagesCount == 3) {
            List(previousPageButton, firstPageButton, middlePageButton, lastPageButton, nextPageButton, infoText)
        } else {
            if(currentPage == 0 || currentPage == pagesCount - 1) {
                List(previousPageButton, firstPageButton, jumpToPageButton1, lastPageButton, nextPageButton, infoText, jumpDiv)
            } else if(currentPage == 1) {
                List(previousPageButton, firstPageButton, currentPageButton, jumpToPageButton2, lastPageButton, nextPageButton, infoText, jumpDiv)
            } else if(currentPage == pagesCount - 2) {
                List(previousPageButton, firstPageButton, jumpToPageButton1, currentPageButton, lastPageButton, nextPageButton, infoText, jumpDiv)
            } else {
                List(previousPageButton, firstPageButton, jumpToPageButton1, currentPageButton, jumpToPageButton2, lastPageButton, nextPageButton, infoText, jumpDiv)
            }
        }

        val container = new Div(buttonsList).setAttribute("style", "width:800px; margin: 0 auto;")
        container.setAttribute("style", "position: relative; left: calc(50% - 200px);")
        container
    }

    private def setEnabledPaginationButtons(previousPageButton: Button, firstPageButton: Button, middlePageButton: Button,
                                            currentPageButton: Button, lastPageButton: Button, nextPageButton: Button): Unit = {

        if(currentPage == 0) {
            previousPageButton.setIsEnabled(false)
            firstPageButton.setIsEnabled(false)
            firstPageButton.setAttribute("style", "background-color: #428bca; opacity: 1;")
        } else if(currentPage == 1) {
            middlePageButton.setIsEnabled(currentPage != 1)
        } else if(currentPage == pagesCount - 1) {
            nextPageButton.setIsEnabled(false)
            lastPageButton.setIsEnabled(false)
            lastPageButton.setAttribute("style", "background-color: #428bca; opacity: 1;")
        }

        currentPageButton.setIsEnabled(false)
        currentPageButton.setAttribute("style", "background-color: #428bca; opacity: 1;")
    }

    private def getPageInfoText: String = {
        "Showing "+currentRecordsCount+" of "+allRecordsCount+" triples"
    }

    private def paginateToPage(goingToPage: Int) {
        if(evaluationId.isDefined) {
            TripleTableTransformator.getCachedPage(evaluationId.get, goingToPage, allowedLinesOnPage) { paginated =>
                updateGraphPage(paginated, true, goingToPage)
            } { error =>
                val modal = new FatalErrorModal(error.toString())
                modal.render()
            }
        }
    }

    /**
     * @return (records on page, pages count, all records count)
     */
    def fillTable(graph: Option[Graph], tableHead: html.Element, tableBody: html.Element, pageNumber: Int): (Int, Int, Int)

    protected def createVertexView(vertex: IdentifiedVertex): View = {
        val dataSourceAnchor = new Anchor(List(new Icon(Icon.hdd)))
        dataSourceAnchor.mouseClicked += { e =>
            vertexBrowsingDataSource.trigger(new VertexEventArgs[this.type](this, vertex))
            false
        }
        val uri = prefixApplier.map(_.applyPrefix(vertex.uri)).getOrElse(vertex.uri)
        val browsingAnchor = new Anchor(List(new Text(uri)))
        browsingAnchor.mouseClicked += { e =>
            vertexBrowsing.trigger(new VertexEventArgs[this.type](this, vertex))
            false
        }

        new Span(List(dataSourceAnchor, new Span(List(new Text(" "))), browsingAnchor))
    }

    protected def createHighlightedVertexView(vertex: IdentifiedVertex, title: String): View = {

        val uri = prefixApplier.map(_.applyPrefix(vertex.uri)).getOrElse(vertex.uri)
        new Span(List(new Icon(Icon.map_marker), new Span(List(new Text(" "))), new Text(uri))).setAttribute("title", title)
    }

    protected def addRow(table: html.Element): html.Element = addElement(table, "tr")

    protected def insertRow(table: html.Element, insertBefore: html.Element): html.Element = insertElement(table, insertBefore, "tr")

    protected def addCell(row: html.Element, isHeader: Boolean = false) = addElement(row, if (isHeader) "th" else "td")

    private def insertElement(parent: html.Element, followingSibling: html.Element, name: String): html.Element = {
        val element = document.createElement[html.Element](name)
        parent.insertBefore(element, followingSibling)
        element
    }

    private def addElement(parent: html.Element, name: String): html.Element = {
        val element = document.createElement[html.Element](name)
        parent.appendChild(element)
        element
    }
}
