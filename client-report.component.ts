import { Component } from '@angular/core';
import { ClientreportService } from '../../../services/clientreport.service';

@Component({
  selector: 'app-client-report',
  standalone: false,
  templateUrl: './client-report.component.html',
  styleUrl: './client-report.component.css'
})
export class ClientReportComponent {

  constructor(private clientReportService : ClientreportService) { }
  
  downloadClientReport(format: string): void {
    this.clientReportService.generateReport(format);
  }

}
