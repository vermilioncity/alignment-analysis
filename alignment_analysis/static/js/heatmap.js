'use strict';

d3.selection.prototype.moveToFront = function () {
    return this.each(function () {
        this.parentNode.appendChild(this);
    });
};

d3.selection.prototype.moveToBack = function () {
    return this.each(function () {
        var firstChild = this.parentNode.firstChild;
        if (firstChild) {
            this.parentNode.insertBefore(this, firstChild);
        }
    });
};


function buildHeatmap() {
    const margin = {top: 20, right: 20, bottom: 50, left: 120};
    const width = 1200 - margin.left - margin.right;
    const height = 900 - margin.top - margin.bottom;

    let svg = d3.select("#heatmap-container").append("svg")
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom)
        .append("g")
        .attr('id', 'inner-heatmap')
        .attr("transform", `translate(${margin.left}, ${margin.top})`);

    let that = {svg, margin, width, height};

    that.render = function () {
        d3.csv('data/heatmap.csv')
            .then(data => this.buildChart(data))
            .catch(error => this.handleError(error));
    };

    that.handleError = function (error) {
        this.svg.append('text')
            .attr("y", this.height / 2)
            .attr("x", (this.width) / 2)
            .text('Error loading data!')
            .style("text-anchor", "middle")
            .style("vertical-anchor", "middle")
            .style("font-family", "sans-serif");

        throw error
    };

    that.buildChart = function (data) {
        const xscale = this.createXScale(data);
        const yscale = this.createYScale(data);
        this.addMajorAxes(this.svg, xscale, yscale);
        this.addMarks(this.svg, data, xscale, yscale, this.handleMouseEnter, this.handleMouseLeave);
    };

    that.createXScale = (data) => {
        return d3.scaleBand()
            .domain(getNames(data))
            .range([0, width])
            .padding(0.01)
    };

    that.createYScale = (data) => {
        return d3.scaleBand()
            .domain(getNames(data))
            .range([height, 0])
    };

    that.addXAxisTitle = function (svg, height, width, margin) {
        svg.append("text")
            .attr("transform",
                `translate(${width / 2}, ${(height + margin.top + 20)})`)
            .text('Evil vs. Good')
            .style("text-anchor", "middle")
            .style('font-family', 'sans-serif');
    };

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

    that.addMajorAxes = function (svg, xscale, yscale) {
        const xAxis = d3.axisBottom()
            .scale(xscale);

        svg.append('g')
            .classed('x.axis', true)
            .attr('transform', `translate(0,${height})`)
            .call(xAxis)
            .selectAll("text")
        .style("text-anchor", "end")
        .attr("dx", "-.8em")
        .attr("dy", ".15em")
        .attr("transform", "rotate(-90)");

        const yAxis = d3
            .axisLeft()
            .scale(yscale);

        svg.append('g')
            .classed('y.axis', true)
            .call(yAxis);
    };

that.addMarks = function (svg, data, xscale, yscale, handleMouseEnter, handleMouseLeave) {
    const group = svg.append('g').attr('id', 'markArea');
    const color = d3.scaleSequential()
        .interpolator(d3['interpolatePiYG'])
        .domain([-1, 1]);

    return group.selectAll('rect')
        .data(data)
        .join('rect')
        .attr('r', 5)
        .attr('x', (d) => xscale(d['Name']))
        .attr('y', (d) => yscale(d['Comparison']))
        .attr('data-name', (d) => d.Name)
        .attr("width", xscale.bandwidth())
        .attr("height", yscale.bandwidth())
        .style("fill", (d) => color(d.Score))
        //.on('mouseenter', handleMouseEnter)
       // .on('mouseleave', handleMouseLeave)

};

that.handleMouseEnter = function (data) {
    const rect = this;
    const name = data.Name;

    d3.select(rect).style('fill', 'orange')
        .attr('r', 8)
        .attr('class', 'active')
        .moveToFront();

    let svg = d3.select("#inner-heatmap");

    svg.append('text').attr('x', rect.cx.animVal.value - 30)
        .attr('y', rect.cy.animVal.value - 15)
        .attr('data-name', name)
        .text(parseFloat(data.Score).toFixed(2))
        .style('font-family', 'sans-serif')
        .style('font-size', '10px')
        .style('font-weight', 'bold')
};

that.handleMouseLeave = function (d) {
    d3.select(rect)
        .style('fill', 'black')
        .attr('r', 5)
        .moveToBack()
        .classed("active", false);

    let svg = d3.select("#heatmap-container > svg > g");
    svg.select(`text[data-name='${name}']`).remove();

};


that.getMinMaxBounds = function (data, column) {
    let minBound, maxBound;
    [minBound, maxBound] = d3.extent(data, (d) => parseFloat(d[column]));

    minBound = minBound > 0 ? minBound * 0.9 : minBound * 1.1;
    maxBound = maxBound > 0 ? maxBound * 1.1 : maxBound * 0.9;

    return [minBound, maxBound]
};

that.addXAxisTitle(that.svg, that.height, that.width, that.margin);
that.addYAxisTitle(that.svg, that.height, that.margin);

return that
}

function getNames(data) {
    return data.map(d => d.Name)
}