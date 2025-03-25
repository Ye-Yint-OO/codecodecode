import { Component } from '@angular/core';
import { BranchreportService } from '../../../services/branchreport.service';

@Component({
  selector: 'app-branch-report',
  standalone: false,
  templateUrl: './branch-report.component.html',
  styleUrl: './branch-report.component.css'
})
export class BranchReportComponent {

  constructor(private branchReportService : BranchreportService) { }

  downloadReport(format: string): void {
    this.branchReportService.generateReport(format);
  }

}
