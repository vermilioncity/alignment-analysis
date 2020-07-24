import {Scatterplot} from "./scatterplot.js";
import {addSelectFields, renderOptions} from "./selector.js";
import SlimSelect from 'slim-select';
import {highlightScatterplotByPersonSelection} from "./scatterplot";

/** Keeps track of user data choices and lets subscribers know to update on changes */
function Context() {
    this.data = {
        respondent: [],
        team: [],
        location: []
    };

    /**
    * Adds select element to list of subscribers and adds event listener to call context's update on change.
    * @param {SlimSelect} element
    */
    this.subscribe = function (element) {
        element.addEventListener('change', this.update);
        this.subscribers.push(element);
    };

    this.subscribers = [];

    /**
    * Updates stored parameters according to selections made for a given select element (e.g., respondents),
    * and then publishes updated parameters to other subscribers.
    * @param {event} event
    */
    this.update = async function (event) {
        const element = event.target;
        const type = this._getType(element);
        const params = {[type]: element.slim.selected()};

        this.data = {...this.data, ...params};

        const newSubs = this.subscribers.slice();
        await this.publish(type);
        this.subscribers = newSubs;

    }.bind(this);

    /**
    * For all other subscribers, update their options to filter to the newly selected choices
    * (e.g., if "New York" was selected for location, update respondents to show only those in that location
    * @param {string} type
    */
    this.publish = async function (type) {

        const len = this.subscribers.length-1;

        let choices = [];
        for (let i = len; i > -1; i--) {
            let subscriber = this.subscribers[i];
            let type_ = this._getType(subscriber);
            if (type_ !== type) {
                let slim = subscriber.slim;
                const currentChoices = slim.selected();
                this.subscribers.pop(i);
                await renderOptions(type_, this.data);
                choices.push(currentChoices);
            }
        }

        Scatterplot.render(this.data);

    };

    /**
    * Get type of element (e.g., respondent/team/location)
    * @param {SlimSelect} element
    * @return {string} the "type" of selection
    */
    this._getType = (element) => (element.id.split('-')[0]);
}



import React from 'react';
import ReactDOM from 'react-dom'
import Select from 'react-select';


const options =  [{label: 'Group 1', value: 'group_1', children: [{ label: "Option 1", value: "value_1"}, { label: "Option 2", value: "value_2" }]},
                  {label: 'Group 2', value: 'group_2', children: [{ label: "Option 3", value: "value_3"}, { label: "Option 4", value: "value_4" }]}];

const m = {'group_1': {label: 'Group 1', value: 'group_1', children: ['value_1', 'value_2']},
           'value_1': {label: 'Value 1', value: 'value_1'},
           'value_2': {label: 'Value 2', value: 'value_2'},
           'group_2': {label: 'Group 2', value: 'group_2', children: ['value_3', 'value_4']},
           'value_3': {label: 'Value 3', value: 'value_3'},
           'value_4': {label: 'Value 4', value: 'value_4'}};


const customStyle = {
    option: (provided, { data }) => ({
        ...provided,
        "padding-left": data.children ? 10 : 25,
        "font-family": "sans-serif",
        "font-weight": data.children ? "bold" : "regular",
    }),
    multiValueLabel: (provided, { data }) => ({
        ...provided,
        "font-family": "sans-serif",

}),
};

class App extends React.Component {

  state = {
    selectedOption: null
  };

  handleChange = (selectedOption, actionMeta) => {

    this.setState(
        {selectedOption},
      () => console.log('Option selected', this.state.selectedOption, actionMeta)
    );
  };
  render() {
    const { selectedOption } = this.state;

    return (
      <Select
        value={selectedOption}
        hideSelectedOptions={true}
        isMulti={true}
        onChange={this.handleChange}
        options={Object.values(m)}
        styles={customStyle}
      />
    );
  }
}
ReactDOM.render(
  <App />,
  document.getElementById('root')
);


const context = new Context();

Scatterplot.render(context.data);
addSelectFields(context);
