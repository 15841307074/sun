      #有奖问答 str
      activity_answer_record:
        actualDataNodes: db_master.activity_answer_record_${0..9}
        tableStrategy:
          standard:
            shardingColumn: member_mobile
            shardingAlgorithmName: activity-answer-record-inline
      activity_answer_record_detail:
        actualDataNodes: db_master.activity_answer_record_detail_${0..9}
        tableStrategy:
          standard:
            shardingColumn: member_mobile
            shardingAlgorithmName: activity-answer-record-detail-inline
      activity_answer_reward_log:
        actualDataNodes: db_master.activity_answer_reward_log_${0..9}
        tableStrategy:
          standard:
            shardingColumn: member_mobile
            shardingAlgorithmName: activity-answer-reward-log-inline
      activity_answer_task:
        actualDataNodes: db_master.activity_answer_task_${0..9}
        tableStrategy:
          standard:
            shardingColumn: member_mobile
            shardingAlgorithmName: activity-answer-task-inline
      #有奖问答 end
	  
	  
	        #有奖问答 str
      activity-answer-record-inline:
        type: INLINE
        props:
          algorithm-expression: activity_answer_record_${member_mobile % 10}    
      activity-answer-record-detail-inline:
        type: INLINE
        props:
          algorithm-expression: activity_answer_record_detail_${member_mobile % 10}
      activity-answer-reward-log-inline:
        type: INLINE
        props:
          algorithm-expression: activity_answer_reward_log_${member_mobile % 10}    
      activity-answer-task-inline:
        type: INLINE
        props:
          algorithm-expression: activity_answer_task_${member_mobile % 10}     
      #有奖问答 end