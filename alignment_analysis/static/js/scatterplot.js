import * as d3 from 'd3';
import "../css/index.css";
import {getElementsToActivate, getActiveElements} from './utils.js';
import {buildUrl} from "./utils";

'use strict';

class SampleError extends Error {
  constructor(message) {
    super(message);
    this.name = "SampleError";
  }
}

/**
* Moves an element to the front.
*/
d3.selection.prototype.moveToFront = function () {
    return this.each(function () {
        this.parentNode.appendChild(this);
    });
};

/**
* Moves an element to the back.
*/
d3.selection.prototype.moveToBack = function () {
    return this.each(function () {
        var firstChild = this.parentNode.firstChild;
        if (firstChild) {
            this.parentNode.insertBefore(this, firstChild);
        }
    });
};


/**
* Creates and destroys a scatterplot object.
*/
var Scatterplot = (function () {
    var instance;

    return {
        render: function (params) {
            if (instance) {
                instance.clear();
            }

            instance = buildScatterPlot();
            instance.render(params);

            return instance;
        }
    };
})();


/**
* Creates a scatterplot object.
*/
function buildScatterPlot() {

    const margin = {top: 20, right: 20, bottom: 50, left: 70};
    const width = 800 - margin.left - margin.right;
    const height = 600 - margin.top - margin.bottom;

    let svg = d3.select("#scatterplot-container").append("svg")
                 .attr("width", width + margin.left + margin.right)
                 .attr("height", height + margin.top + margin.bottom)
                 .append("g")
                 .attr('id', 'inner-scatterplot')
                 .attr("transform", `translate(${margin.left}, ${margin.top})`);

    let that = {svg, margin, width, height};

    /**
    * Clears the scatterplot of data.
    */
    that.clear = () => {
        const svg = document.querySelector('#scatterplot-container > svg');
        svg.parentElement.removeChild(svg);
    };

    /**
    * Populates the scatterplot with data.
    */
    that.render = function (params) {
        const url = buildUrl('z_scores', params);
        fetch(url).then(response => response.json())
            .then(data => this.validateData(data))
            .then(data => this.buildChart(data))
            .catch(error => this.handleError(error));
    };

    that.validateData = function (data) {
        if (data.length < 5) {
            throw new SampleError('Low sample; choose less restrictive filters.');
        }

        return data
    };

    /**
    * Displays an error message in the event that an error is raised.
    * @param {error} error
    */
    that.handleError = function (error) {
        const message = (error.name === 'SampleError') ? error.message : 'Error loading data!';

        this.svg.append('text')
            .attr("y", this.height / 2)
            .attr("x", (this.width) / 2)
            .text(message)
            .style("text-anchor", "middle")
            .style("vertical-anchor", "middle")
            .style("font-family", "sans-serif");

        throw error
    };

    /**
    * Set up a chart's axes, titles, and data points.
    * and then publishes updated parameters to other subscribers.
    * @param {obj} data Data points by respondent
    */
    that.buildChart = function (data) {
        const xscale = this.createXScale(data);
        const yscale = this.createYScale(data);
        this.addMajorAxes(this.svg, xscale, yscale);
        this.addMidAxes(this.svg, xscale, yscale);
        this.addDimensionTitles(this.svg, this.height, this.width, yscale, xscale);
        this.addMarks(this.svg, data, xscale, yscale, this.handleMouseEnter, this.handleMouseLeave);

    };

    /**
    * Set x-axis min/max bounds according to data parameters
    * @param {obj} data Data points by respondent
    */
    that.createXScale = (data) => {
        return d3.scaleLinear()
            .domain(that.getMinMaxBounds(data, 'Evil vs. Good'))
            .range([0, width])
    };

    /**
    * Set y-axis min/max bounds according to data parameters
    * @param {obj} data Data points by respondent
    */
    that.createYScale = (data) => {
        return d3.scaleLinear()
            .domain(that.getMinMaxBounds(data, 'Chaotic vs. Lawful'))
            .range([height, 0])
    };

    /**
    * Set x-axis title
    * @param {svg} svg
    * @param {number} height
    * @param {number} width
    * @param {number} margin
    */
    that.addXAxisTitle = function (svg, height, width, margin) {
        svg.append("text")
            .attr("transform",
                `translate(${width / 2}, ${(height + margin.top + 20)})`)
            .text('Evil vs. Good')
            .style("text-anchor", "middle")
            .style('font-family', 'sans-serif');
    };

    /**
    * Set y-axis title
    * @param {svg} svg
    * @param {number} height
    * @param {number} margin
    */
    that.addYAxisTitle = function (svg, height, margin) {
        svg.append("text")
            .attr("transform", "rotate(-90)")
            .attr("y", 0 - margin.left)
            .attr("x", 0 - (height / 2))
            .attr("dy", "1em")
            .text('Chaotic vs. Lawful')
            .style("text-anchor", "middle")
            .style('font-family', 'sans-serif');
    };

    /**
    * Add all major axes (y and x), as well as intermediary axes
    * @param {svg} svg
    * @param {number} xscale
    * @param {number} yscale
    */
    that.addMajorAxes = function (svg, xscale, yscale) {
        const xAxis = d3.axisBottom()
            .scale(xscale);

        svg.append('g')
            .classed('x.axis', true)
            .attr('transform', `translate(0,${height})`)
            .call(xAxis);

        const yAxis = d3
            .axisLeft()
            .scale(yscale);

        svg.append('g')
            .classed('y.axis', true)
            .call(yAxis);
    };

    that.addMidAxes = function (svg, xscale, yscale) {
        const yMidAxis = d3.axisLeft()
            .ticks(0)
            .tickSizeOuter(0)
            .scale(yscale);

        svg.append('g')
            .classed('y2.axis', true)
            .attr('transform', `translate(${xscale(0)}, 0)`, 0)
            .call(yMidAxis);

        const xMidAxis = d3.axisBottom()
            .ticks(0)
            .tickSizeOuter(0)
            .scale(xscale);

        svg.append('g')
            .classed('x2.axis', true)
            .attr('transform', `translate(0,${yscale(0)})`)
            .call(xMidAxis);

        return svg
    };

    /**
    * Add titles to quadrants (e.g., Chaotic/Evil in lower left)
    * @param {svg} svg
    * @param {number} height
    * @param {number} width
    * @param {} yscale
    * @param {} xscale
    */
    that.addDimensionTitles = function (svg, height, width, yscale, xscale) {
        for (let alignment of ['Chaotic/Good', 'Chaotic/Evil', 'Lawful/Evil', 'Lawful/Good']) {
            let coords = this.getQuadrantCoordinates(alignment, height, width, yscale, xscale);

            svg.append('text')
                .text(alignment)
                .attr('transform', `translate(${coords.left}, ${coords.top})`)
                .style('text-anchor', 'middle')
                .style('vertical-align', 'middle')
                .style('font-family', 'sans-serif')
                .style('font-size', 30)
                .style('fill', '#c2cdd3');
        }
    };

    /**
    * Calculate the coordinates of a text label based on the scale of the graph.
    * @param {string} text
    * @param {number} height
    * @param {number} width
    * @param {} yscale
    * @param {} xscale
    */
    that.getQuadrantCoordinates = function (text, height, width, yscale, xscale) {

        let coords = {};

        if (text.indexOf('Lawful') > -1) {
            coords.top = yscale(0) / 2;
        } else {
            coords.top = (height + yscale(0)) / 2;
        }

        if (text.indexOf('Good') > -1) {
            coords.left = (width + xscale(0)) / 2;
        } else {
            coords.left = xscale(0) / 2;
        }

        return coords

    };

    /**
    * Populate the graph with circles for each data point
    * @param {svg} svg
    * @param {object} data
    * @param {} xscale
    * @param {} yscale
    * @param {function} handleMouseEnter
    * @param {function} handleMouseLeave
    */
    that.addMarks = function (svg, data, xscale, yscale, handleMouseEnter, handleMouseLeave) {
        const group = svg.append('g').attr('id', 'markArea');
        return group.selectAll('circle')
            .data(data)
            .join('circle')
            .attr('r', 5)
            .attr('cx', (d) => xscale(d['Evil vs. Good']))
            .attr('cy', (d) => yscale(d['Chaotic vs. Lawful']))
            .attr('data-name', (d) => d.name)
            .on('mouseenter', handleMouseEnter)
            .on('mouseleave', handleMouseLeave)
    };

    /**
    * Add scatterplot highlight to node
    * @param {object} d
    */
    that.handleMouseEnter = function (d) {
        addScatterplotHighlight(this, d.name);
    };

    /**
    * Remove scatterplot highlight from node
    * @param {object} d
    */
    that.handleMouseLeave = function (d) {
        removeScatterplotHighlight(this, d.name);
    };

    /**
    * Calculate the bounds of an axis based on the minimum and maximum data points
    * @param {object} data
    * @param {} column
    * @return {[number]} Minimum and maximum bounds
    */
    that.getMinMaxBounds = function (data, column) {
        let minBound, maxBound;
        [minBound, maxBound] = d3.extent(data, (d) => parseFloat(d[column]));

        minBound = minBound > 0 ? minBound * 0.9 : minBound * 1.1;
        maxBound = maxBound > 0 ? maxBound * 1.1 : maxBound * 0.9;

        return [minBound, maxBound]
    };

    that.addXAxisTitle(that.svg, that.height, that.width, that.margin);
    that.addYAxisTitle(that.svg, that.height, that.margin);

    return that;
}

/**
* Change color and font / add label to a node
* @param {element} circle
* @param {string} name
*/
function addScatterplotHighlight(circle, name) {

    d3.select(circle).style('fill', 'orange')
        .attr('r', 8)
        .attr('class', 'active')
        .moveToFront();

    let svg = d3.select("#inner-scatterplot");

    svg.append('text').attr('x', circle.cx.animVal.value - 30)
        .attr('y', circle.cy.animVal.value - 15)
        .attr('data-name', name)
        .text(name)
        .style('font-family', 'sans-serif')
        .style('font-size', '10px')
        .style('font-weight', 'bold')
}

/**
* Change color and font / remove label from a node
* @param {element} circle
* @param {string} name
*/
function removeScatterplotHighlight(circle, name) {
    d3.select(circle)
        .style('fill', 'black')
        .attr('r', 5)
        .moveToBack()
        .classed("active", false);

    let svg = d3.select("#scatterplot-container > svg > g");
    svg.select(`text[data-name='${name}']`).remove();
}

/**
* Iterate over selections in an event, removing highlighting from no longer relevant nodes and highlighting
* new relevant nodes as necessary
* @param {event} e
*/
function highlightScatterplotByPersonSelection(e) {
    let selectionNodes = new Set(getElementsToActivate('circle', e.target.selectedOptions));
    let currentlyHighlighted = getActiveElements('circle');
    currentlyHighlighted = new Set(currentlyHighlighted);

    currentlyHighlighted.forEach((n) => {
            if (!selectionNodes.has(n)) {
                const dataName = n.getAttribute("data-name");
                removeScatterplotHighlight(n, dataName);
            }
        }
    );

    selectionNodes.forEach((s) => {
        if (!currentlyHighlighted.has(s)) {
            const dataName = s.getAttribute("data-name");
            addScatterplotHighlight(s, dataName);
        }
    })
}

export {Scatterplot};
export {highlightScatterplotByPersonSelection};