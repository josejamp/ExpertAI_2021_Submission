# CRTPT0SEN

##  Introduction
Crypt0Sen is a cryptocurrency monitoring system using sentiment analysis and entity recognition. Its goal is to track what is being said about different cryptocurrencies on multiple internet forums and social networks, and present the information on a dashboard, so that the user can easily check the state of affairs of his favourite cryptocurrencies.

## Applied Categories
The following category applies to our project:
- Identifying Sentiment of Brands, Companies, or Products

## System description
Crypt0Sen consists of the following independent modules:
- Crawlers: multiple crawlers operate on different cryptocurrency related subreddits, the Yahoo Finance forums and checks on the Twitter account of cryptocurrency related personalities (Elon Musk). These crawlers were made in Java and run every four hours, saving the extracted information in json files.
- Sentiment and entity extraction: a batch process runs periodically and makes a full language analysis of the extracted texts using the Expert.ai API. The full analysis is stored in a json file.
- Elastic indexing:
- Kibana dashboard:

## Technologies
- Java: for the crawler code and Expert.ai API calls
- Python:
- 
