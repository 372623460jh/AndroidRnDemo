'use strict';

import React, { Component } from 'react';
import {
  AppRegistry,
  StyleSheet,
  Text,
  View,
  TouchableNativeFeedback,
  NativeModules,
} from 'react-native';

if (!__DEV__) {
    global.console = {
        info: () => {
        },
        log: () => {
        },
        warn: () => {
        },
        debug: () => {
        },
        error: () => {
        },
    };
}

class Hello extends Component {
  render() {
    return (
      <View style={styles.container}>
         <Text style={{margin: 30}}>我是更新后的页面</Text>
         <TouchableNativeFeedback
              onPress={this._onPressButton}
              background={TouchableNativeFeedback.SelectableBackground()}>
            <View style={{width: 150, height: 100, backgroundColor: 'blue'}}>
              <Text style={{margin: 30}}>我是更新后的按钮</Text>
            </View>
          </TouchableNativeFeedback>
      </View>
    );
  }

  _onPressButton(){
      NativeModules.CommonModule.hotUpdate("Hello");
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
  instructions: {
    textAlign: 'center',
    color: '#333333',
    marginBottom: 5,
  },
});

AppRegistry.registerComponent('Hello', () => Hello);