package com.project.helpzoo.funding.model.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.springframework.stereotype.Repository;

import com.project.helpzoo.funding.dto.FundingDetailViewDto;
import com.project.helpzoo.funding.dto.FundingMainViewDto;
import com.project.helpzoo.funding.model.vo.funding.FundingProject;
import com.project.helpzoo.funding.model.vo.funding.QFundingCategory;
import com.project.helpzoo.funding.model.vo.funding.QFundingMaker;
import com.project.helpzoo.funding.model.vo.funding.QFundingProject;
import com.project.helpzoo.funding.model.vo.funding.QReward;
import com.project.helpzoo.funding.model.vo.order.QOrderReward;
import com.project.helpzoo.funding.model.vo.search.FundingSearch;
import com.project.helpzoo.funding.model.vo.search.SearchSort;
import com.project.helpzoo.funding.model.vo.search.SearchStatus;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

@Repository
@Transactional
public class FundingDAO {

	@PersistenceContext
	private EntityManager em;
	
	


	/** 처음 펀딩 페이지에 왔을때 검색 List를 반환하는 메소드 입니다.
	 * @param cp
	 * @param fundingSearch
	 * @return
	 */
	public List<FundingMainViewDto> selectList(int cp, FundingSearch fundingSearch) {


		
		JPAQueryFactory query = new JPAQueryFactory(em);

		QFundingProject funding = QFundingProject.fundingProject;

		QFundingCategory category = QFundingCategory.fundingCategory;

		QFundingMaker maker = QFundingMaker.fundingMaker;

		QReward reward = QReward.reward;

		QOrderReward orderReward = QOrderReward.orderReward;
		
		List<Tuple> result = query
				.select(funding.id,
						funding.title, category.category_name, maker.name, funding.goalAmount, reward.price,
						funding.summary,
						funding.readCount,funding.likeCount,
						orderReward.count.sum())
				.from(funding)
				.leftJoin(funding.reward, reward)
				.leftJoin(orderReward).on(reward.id.eq(orderReward.reward.id))
				.leftJoin(funding.category, category)
				.leftJoin(funding.fundingMaker, maker)
				.groupBy(funding.id,funding.title, category.category_name, maker.name, funding.goalAmount, 
						reward.price,funding.readCount,funding.likeCount
						,funding.summary)
				.orderBy(orderby(fundingSearch.getSearchSort()))
				.offset(0)
				.limit(10)
				.fetch();
		
		
		List<FundingMainViewDto> mainViewList = new ArrayList<FundingMainViewDto>();
		
		
		
		for (Tuple tuple : result) {
			int totalOrderAmount = 0;
		if(  tuple.get(orderReward.count.sum())!= null && tuple.get(reward.price)!=null ) {
		totalOrderAmount =  tuple.get(orderReward.count.sum())*tuple.get(reward.price);
		}
		FundingMainViewDto mainView = new FundingMainViewDto
			(
			tuple.get(funding.id),
			tuple.get(funding.title),
			tuple.get(category.category_name), 
			tuple.get(funding.summary),
			tuple.get(funding.fundingMaker.name), 
			totalOrderAmount, 
			tuple.get(funding.goalAmount), 
			tuple.get(funding.endDay)
			,(int)(((double)totalOrderAmount/tuple.get(funding.goalAmount))*100)
			);
		
		
		mainViewList.add(mainView);
		}

		return mainViewList;
	}
	
	
	

	/** 정렬조건  querydsl 설정 메소드 입니다.
	 * -jisan-
	 * @param searchSort
	 * @return
	 */
	private OrderSpecifier<Integer> orderby(SearchSort searchSort) {

		if (searchSort == searchSort.LIKE) {

			return QFundingProject.fundingProject.likeCount.desc();
		} else if (searchSort == searchSort.VIEW) {

			return QFundingProject.fundingProject.readCount.desc();
		} else if (searchSort == searchSort.TIME) {

			return null;
		}
		return QFundingProject.fundingProject.likeCount.desc();

	}
	

	/** 검색조건 설정 querydsl 메소드 입니다.
	 * @param searchStatus
	 * @return
	 */
	
	private BooleanExpression statusEq(SearchStatus searchStatus) {
		if (searchStatus == searchStatus.ALL) {

			return null;
		} else if (searchStatus == searchStatus.PROCEEDING) {

			return QFundingProject.fundingProject.status.eq("Y");
		} else if (searchStatus == searchStatus.TERMINATED) {

			return QFundingProject.fundingProject.status.eq("N");
		}

		return null;
	}

	
	
	
	
	/** 검색 텍스트 탐색 querydsl 메소드 입니다.
	 * @param searchText
	 * @return
	 */
	private BooleanExpression textEq(String searchText) {
			
		if(searchText == null) {
			return null;
			
		}
		
		return QFundingProject.fundingProject.title.like(searchText);
		
	}
	
	
	

	
	/**12시 이후 서버에서 프로젝트 마감기한이 지난 프로젝트를 자동으로 종료처리 하는 메소드입니다.
	 * @return
	 */
	public List<FundingProject> fundingStatusChange() {

		return em.createQuery("select f from FundingProject f where f.status = 'Y' ", FundingProject.class).getResultList();

	}




	public FundingDetailViewDto selectFunding(int fundingNo) {
		

		
		JPAQueryFactory query = new JPAQueryFactory(em);

		QFundingProject funding = QFundingProject.fundingProject;

		QFundingCategory category = QFundingCategory.fundingCategory;

		QFundingMaker maker = QFundingMaker.fundingMaker;

		QReward reward = QReward.reward;

		QOrderReward orderReward = QOrderReward.orderReward;
		
		
		
			Long no = Long.valueOf(fundingNo);

			
			List<Tuple> result  = query
					.select(funding.id,
							funding.story, 
							funding.goalAmount,
							reward.id,
							reward.title, 
							reward.content, 
							reward.price, 
							reward.amount,
							maker.name,
							maker.sns,
							funding.readCount,
							funding.likeCount,
							maker.kakaoURL,
							funding.endDay,
							funding.startDay,
							orderReward.count.sum())
					.from(funding)
					.leftJoin(funding.reward, reward)
					.leftJoin(funding.fundingMaker, maker)
					.leftJoin(orderReward).on(reward.id.eq(orderReward.reward.id))
					.where(funding.id.eq(no))
					.groupBy(funding.id,
							funding.story, 
							funding.goalAmount,
							reward.id,
							reward.title, 
							reward.content, 
							reward.price, 
							reward.amount,
							maker.name,
							maker.sns,
							funding.readCount,
							funding.likeCount,
							maker.kakaoURL,
							funding.endDay,
							funding.startDay)
					.fetch();
		
			
			
			Optional<String[]> member = Optional.empty();  //서포터
			
			FundingDetailViewDto detailView = null;
			
		for (Tuple tuple : result) {
		
			int totalOrderAmount = 0;
			
			if(  tuple.get(orderReward.count.sum())!= null && tuple.get(reward.price)!=null ) {
			totalOrderAmount =  tuple.get(orderReward.count.sum())*tuple.get(reward.price);
			}
			
			System.out.println(
			tuple.get(reward.id) +" "+
			tuple.get(orderReward.count.sum())
			);
			
			
			
			detailView = new FundingDetailViewDto(
					tuple.get(funding.story), tuple.get(reward.rewardSeq), 
					
					tuple.get(reward.content)	, tuple.get(reward.title), 
					
					tuple.get(reward.price),tuple.get(reward.amount),
					
					tuple.get(funding.goalAmount),
					
					totalOrderAmount, (int)(((double)totalOrderAmount/tuple.get(funding.goalAmount))*100), 
					
					member, //현재 서포터 값을 선언하지 않았기 때문에 Optional로 널처리 함.
					
					tuple.get(maker.name), 
					tuple.get(maker.sns), 
					tuple.get(maker.kakaoURL)
					,tuple.get(funding.likeCount)
					,tuple.get(funding.endDay),
					tuple.get(funding.startDay)
					);
		
			
		}
		
		
		
		return detailView;
	}

}
