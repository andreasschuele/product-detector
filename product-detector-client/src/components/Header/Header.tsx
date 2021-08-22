import * as React from "react";
import './Header.css';

export interface HeaderProps { 
    title: string;
}

export class Header extends React.Component<HeaderProps, {}> {

    static defaultProps : HeaderProps = {
        title: 'Title',
    }

    render() {
        return (
            <div className="Header">
                 <img src="logo.svg" className="Header-logo" alt="Logo" />
                 <div>
                    <h1>{this.props.title}</h1>
                </div>
            </div>
        );
    }
}