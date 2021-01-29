import { Component, OnInit, Input, OnChanges, SimpleChanges, SimpleChange, ViewChild } from '@angular/core';
import { Category } from '../category';
import { CategoriesComponent } from '../categories.component';
import { ChartOptions, ChartType, ChartDataSets } from 'chart.js';
import { BaseChartDirective, Label } from 'ng2-charts';

@Component({
  selector: 'app-category-breakdown',
  templateUrl: './category-breakdown.component.html',
  styleUrls: ['./category-breakdown.component.css']
})
export class CategoryBreakdownComponent implements OnInit, OnChanges {
  barChartOptions: ChartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    scales: {
      xAxes: [{
        ticks: {
          beginAtZero: true
        }
      }], yAxes: [{}]
    },
  };
  @Input() barChartLabels: Label[];
  @Input() barChartData: ChartDataSets[] = [
    { data: [0, 0, 0, 0], label: '' },
  ];
  barChartType: ChartType = 'horizontalBar';
  barChartLegend = true;
  @ViewChild(BaseChartDirective) chart: BaseChartDirective;

  constructor() { }

  ngOnInit() { }

  ngOnChanges(changes: SimpleChanges): void {
    console.log(changes);
    if (changes.barChartLabels) {
      this.barChartLabels = changes.barChartLabels.currentValue;
    }
    if (changes.barChartData) {
      this.barChartData = changes.barChartData.currentValue;
    }
  }
}
