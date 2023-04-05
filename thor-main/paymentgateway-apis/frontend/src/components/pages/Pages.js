import React from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { DataGrid } from '@mui/x-data-grid';
import "./pages.css";
import Graph from '../graph/Graph';

export default function Pages(){


  const columns = [
    { field: 'id', headerName: 'ID' },
    { field: 'firstName', headerName: 'First name' },
    { field: 'lastName', headerName: 'Last name' },
    {
      field: 'age',
      headerName: 'Age',
      type: 'number',
      width: 90,
    },
    {
      field: 'fullName',
      headerName: 'Full name',
      description: 'This column has a value getter and is not sortable.',
      sortable: false,
      width: 160,
      valueGetter: (params) =>
        `${params.getValue(params.id, 'firstName') || ''} ${
          params.getValue(params.id, 'lastName') || ''
        }`,
    },
  ];
  
  const rows = [
    { id: 1, lastName: 'Snow', firstName: 'Jon', age: 35 },
    { id: 2, lastName: 'Lannister', firstName: 'Cersei', age: 42 },
    { id: 3, lastName: 'Lannister', firstName: 'Jaime', age: 45 },
    { id: 4, lastName: 'Stark', firstName: 'Arya', age: 16 },
    { id: 5, lastName: 'Targaryen', firstName: 'Daenerys', age: null },
    { id: 6, lastName: 'Melisandre', firstName: null, age: 150 },
    { id: 7, lastName: 'Clifford', firstName: 'Ferrara', age: 44 },
    { id: 8, lastName: 'Frances', firstName: 'Rossini', age: 36 },
    { id: 9, lastName: 'Roxie', firstName: 'Harvey', age: 65 },
  ];

    const data = [
        {
          name: '00:00',
          uv: 4000,
          pv: 2400,
          amt: 2400,
        },
        {
          name: '01:00',
          uv: 3000,
          pv: 1398,
          amt: 2210,
        },
        {
          name: '02:00',
          uv: 2000,
          pv: 9800,
          amt: 2290,
        },
        {
          name: '03:00',
          uv: 2780,
          pv: 3908,
          amt: 2000,
        },
        {
          name: '04:00',
          uv: 1890,
          pv: 4800,
          amt: 2181,
        },
        {
          name: '05:00',
          uv: 2390,
          pv: 3800,
          amt: 2500,
        },
        {
          name: '06:00',
          uv: 3490,
          pv: 4300,
          amt: 2100,
        },

        {
            name: '07:00',
            uv: 4000,
            pv: 2400,
            amt: 2400,
          },
          {
            name: '08:00',
            uv: 3000,
            pv: 1398,
            amt: 2210,
          },
          {
            name: '09:00',
            uv: 2000,
            pv: 9800,
            amt: 2290,
          },
          {
            name: '10:00',
            uv: 2780,
            pv: 3908,
            amt: 2000,
          },
          {
            name: '11:00',
            uv: 1890,
            pv: 4800,
            amt: 2181,
          },
          {
            name: '12:00',
            uv: 2390,
            pv: 3800,
            amt: 2500,
          },
          {
            name: '01:00',
            uv: 3490,
            pv: 4300,
            amt: 2100,
          },

      ];
    return (
        <div className={"zk-right_container"}>
            <ResponsiveContainer width={800} height={300}>
                <LineChart
                  width={500}
                  height={300}
                  data={data}
                  margin={{top: 5,right: 30,left: 20,bottom: 5}}
                >
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="name" />
                    <YAxis />
                    <Tooltip />
                    <Legend />
                    <Line type="monotone" dataKey="pv" stroke="#8884d8" activeDot={{ r: 8 }} />
                    <Line type="monotone" dataKey="uv" stroke="#82ca9d" />
                </LineChart>
            </ResponsiveContainer>
          <Graph dataColumn={columns} dataRow={rows} />
      

        </div>
    )
}