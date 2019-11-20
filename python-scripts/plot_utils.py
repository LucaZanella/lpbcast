import pandas as pd
import seaborn as sns
import matplotlib.pyplot as plt
import os.path
from os import path
sns.set(color_codes=True)


def plot_view_distribution(file_name, num_of_processes, df_name):
    if path.exists("data/" + df_name):
        view_distribution_df = pd.read_pickle("data/" + df_name)
    else:
        view_distribution_df = pd.read_csv("data/" + file_name)

        for i in range(0, num_of_processes):
            column_name = 'process_' + str(i)
            view_distribution_df[column_name] = view_distribution_df['view_distribution'] \
                .apply(lambda row: int(str(row)[1:-1].split(',')[i]))
        #    n_bins = max(view_distribution[column_name])
        #    sns.distplot(view_distribution[column_name], kde=False, bins=n_bins)
        #    fig_name = "figures/process_" + str(i) + ".png"
        #    plt.tight_layout()
        #    plt.savefig(fig_name)
        #    plt.close()

        view_distribution_df.to_pickle("data/" + df_name)

    sns.distplot(view_distribution_df.iloc[:,2:].mean())
    plt.tight_layout()
    plt.show()
    plt.savefig('figures/mean_view_distribution.png')
    plt.close()


def compute_message_propagation(file_name, df_name):
    message_propagation = pd.read_csv("data/" + file_name)

    # new data frame with split value columns
    message_propagation['message_propagation'] = message_propagation['message_propagation'].str[1:-1].str.split(",")

    event_ids = []

    for row in message_propagation.itertuples():
        for event_propagation in row.message_propagation:
            if event_propagation:
                event_id = event_propagation.split('=')[0]
                #propagation = event_propagation.split('=')[1]
                if event_id not in event_ids:
                    event_ids.append(str(event_id).strip())

    for event_id in event_ids:
        column_name = 'event_' + event_id
        message_propagation[column_name] = message_propagation['message_propagation']\
            .apply(lambda row: get_message_propagation_by_id(row, event_id))

    message_propagation = message_propagation.apply(lambda x : pd.Series(x.dropna().values))
    message_propagation.to_pickle(df_name)


def get_message_propagation_by_id(message_propagation, event_id):
    message_propagation = str(message_propagation)
    event_id = str(event_id)

    if event_id in message_propagation:
        return message_propagation.split(event_id)[1].split("'")[0][1:]
    else:
        return None


def plot_average_message_propagation(df_file_name):
    message_propagation = pd.read_pickle(message_propagation_df_file_name)
    message_propagation['average'] = message_propagation.iloc[:, 2:].apply(pd.to_numeric).mean(numeric_only=True, axis=1)

    sns.lineplot(x='tick', y='average', data=message_propagation)
    plt.tight_layout()
    plt.show()


if __name__ == "__main__":

    num_of_processes = 100
    view_distribution_file_name = 'ModelOutput.2019.nov.20.11_27_25.txt'
    view_distribution_df_file_name = 'view_distribution.pkl'
    message_propagation_file_name = 'ModelOutput.2019.nov.19.09_54_37.txt'
    message_propagation_df_file_name = 'message_propagation.pkl'

    plot_view_distribution(view_distribution_file_name, num_of_processes, view_distribution_df_file_name)

    #compute_message_propagation(message_propagation_file_name, message_propagation_df_file_name)
    #plot_average_message_propagation(message_propagation_df_file_name)