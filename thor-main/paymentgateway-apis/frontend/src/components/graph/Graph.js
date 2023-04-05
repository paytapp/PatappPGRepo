import React from 'react';
import { DataGrid } from '@mui/x-data-grid';


export default function Graph(props){

   

    return (
        <div style={{ height: 400, width: '100%' }}>
            <DataGrid
                  rows={props.dataRow}
                  columns={props.dataColumn}
                  pageSize={5}
                  rowsPerPageOptions={[5]}
                  checkboxSelection
            />
        </div>
    )
}