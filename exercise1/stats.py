from metaflow import FlowSpec, step, IncludeFile, Parameter
import datetime
import re
import os
import pandas


def script_path(filename):
    "Gets the absolute path of a file"
    filepath = os.path.join(os.path.dirname(__file__))
    return os.path.join(filepath, filename)

def read_log(filename):
    """Failsafe way to read CSV"""
    df = pandas.read_csv(script_path(filename), sep='|', usecols=[0,1,2], names=['song_id', 'user_id', 'country'])
    return df.dropna(thresh=3)

class ListeningStatsFlow(FlowSpec):
    """
    A flow to generate statistics about the song listenings

    The flow performs the following steps:
    1) Ingests a CSV into a Pandas Dataframe
    2) Fan-out over group using Metaflow foreach
    3) Compute top K elements for each group
    4) Save a dictionary of group specific statistics
    """
    group = Parameter("group", help="Group by", default="country")
    k = Parameter("k", help="Maximum number of top elements", default=50)

    @step
    def start(self):
        """
        The start step:
        1) Loads the listening metadata into pandas dataframe
        2) Finds all the unique countries and users
        3) Launches parallel statistics computation for each country
        """
        COLS = ['song_id', 'user_id', 'country']
        td = datetime.datetime.today()
        lastWeek = ["listens-%s.log" % (td - datetime.timedelta(i)).strftime('%Y%m%d') for i in range(7)]

        # Test the CSV reading function on a sample file with invalid lines
        # print(read_log("listens-test.log").to_string())

        df0 = pandas.DataFrame(columns=['song_id', 'user_id', 'country', 'listens'])
        for pfile in os.listdir(script_path('.')):
            dateSearch = re.search('listens-\d{4}\d{2}\d{2}.log', pfile)
            if dateSearch:
                  dateFound = dateSearch.group(0)
                  if dateFound in lastWeek:
                      df = read_log(dateFound)
                      df = df.groupby(['song_id', 'user_id', 'country']).size().reset_index(name='listens')
                      df0 = df0.append(df, sort=False)

        self.dataframe = df0

        # Compute statistics for each group in parallel (fan-out)
        self.groups = self.dataframe[self.group].unique()
        self.next(self.compute_statistics, foreach='groups')

    @step
    def compute_statistics(self):
        "Computes statistics for a single group element"
        self.element = self.input
        print("Computing statistics for %s" % self.element)

        # Find all the songs related to this group
        selector = self.dataframe[self.group] == self.element
        self.dataframe = self.dataframe.loc[selector]
        self.dataframe = self.dataframe.groupby(['song_id'])['listens'].sum().reset_index()

        self.top_k = self.dataframe.nlargest(self.k, ['listens'])
        self.next(self.join)

    @step
    def join(self, inputs):
        "Joins parallel branches and merges results into a dictionary"
        self.element_stats = {
            inp.element: {'top_k': inp.top_k} for inp in inputs
        }
        self.next(self.write_top_k)

    @step
    def write_top_k(self):
        "Writes the top K to a text file"
        data = [(element, data['top_k'][['song_id', 'listens']].to_dict('split')['data']) \
            for element, data in self.element_stats.items()]

        pandas.DataFrame([[element, ','.join([d[0]+':'+str(d[1]) for d in data2])] \
            for element, data2 in data]).to_csv(script_path('%s_top_50_20200221.csv' % self.group), \
                sep='|', header=False, index=False)

        self.next(self.end)

    @step
    def end(self):
        "Ends the flow"
        pass


if __name__ == '__main__':
    ListeningStatsFlow()
