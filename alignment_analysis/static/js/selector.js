import SlimSelect from 'slim-select';
import "../css/index.css";
import {highlightScatterplotByPersonSelection} from './scatterplot.js';
import {buildUrl} from './utils.js';

'use strict';


function parseOptions(type, data) {
    if (type === 'team') {
        return parseToOptGroupLabel(data)
    } else {
        return parseToSingleSelectLabel(data)
    }
}

function parseToSingleSelectLabel(data) {

    const options = data.map(d => ({'text': d.name, 'value': d.id}));

    return options;
}

function parseToOptGroupLabel(data) {
    const departments = data;
    let department;

    departments.forEach((d) => {
        if (d.name === 'Media Analytics') {
            department = d
        }
    });

    const options = [];
    department['teams'].forEach(team => {
        if (team.hasOwnProperty('subteams')) {
            const optgroup = {label: team.name, options: []};
            team['subteams'].forEach(subteam => {
                optgroup.options.push({text: subteam.name, value: subteam.id});
            });
            options.push(optgroup);
        } else {
            options.push({text: team.name, value: team.id, innerHTML: `<b>${team.name}</b>`})
        }
    });

    return options
}


function addMultiselect(type) {

    let option = document.createElement("select");

    option.setAttribute('multiple', '');
    option.setAttribute('id', `${type}-choice`);
    option.addEventListener('change', highlightScatterplotByPersonSelection);

    document.getElementById(`${type}-choice-container`)
        .append(option);

    SlimSelect.prototype.existingData = JSON.stringify([]);

    let slim = new SlimSelect({
        select: `#${type}-choice`,
        selectByGroup: true,
        beforeOnChange: function checkValues (info) {
            return !(this.existingData === JSON.stringify(info))
        },
        onChange: function updateValues(info) {
            this.existingData = JSON.stringify(info);
        }
    });

    return option
}


async function renderOptions(type, params) {

    const slim = document.getElementById(`${type}-choice`).slim;
    const url = buildUrl(`${type}s`, params);

    const results = await fetch(url).then(response => response.json());

    const labels = parseOptions(type, results);
    slim.setData(labels);
}

async function addSelectFields(context) {
    const types = ['respondent', 'location', 'team'];

    for (let type of types) {
        const select = await addMultiselect(type);
        await renderOptions(type, context.data, false);
        await context.subscribe(select);
    }
}

export {addSelectFields, renderOptions}