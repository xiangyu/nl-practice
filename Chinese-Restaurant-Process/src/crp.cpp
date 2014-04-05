#include <iostream>
#include <vector>



void crp_sample(double alpha, int customer_n, std::vector< std::vector<int> >& partition)
{
  std::vector<int> tables;
  partition.push_back(0);
  for (int i = 1; i < customer_n; ++i) {
    
